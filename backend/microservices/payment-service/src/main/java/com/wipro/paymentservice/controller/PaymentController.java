package com.wipro.paymentservice.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.RazorpayClient;
import com.wipro.paymentservice.entity.Order;
import com.wipro.paymentservice.entity.OrderStatus;
import com.wipro.paymentservice.entity.Payment;
import com.wipro.paymentservice.entity.PaymentStatus;
import com.wipro.paymentservice.repository.OrdersRepository;
import com.wipro.paymentservice.repository.PaymentsRepository;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final RazorpayClient razorpayClient;
    private final OrdersRepository ordersRepo;
    private final PaymentsRepository paymentsRepo;

    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    public PaymentController(
            RazorpayClient razorpayClient,
            OrdersRepository ordersRepo,
            PaymentsRepository paymentsRepo) {
        this.razorpayClient = razorpayClient;
        this.ordersRepo = ordersRepo;
        this.paymentsRepo = paymentsRepo;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) throws Exception {

        Long registrationId = body.get("registrationId") == null
                ? null
                : Long.valueOf(body.get("registrationId").toString());

        Long eventId = body.get("eventId") == null
                ? null
                : Long.valueOf(body.get("eventId").toString());

        Integer amountRupees = Integer.valueOf(body.get("amountRupees").toString());
        int amountPaise = amountRupees * 100;

        org.json.JSONObject req = new org.json.JSONObject();
        req.put("amount", amountPaise);
        req.put("currency", "INR");
        req.put("receipt", "rcpt_" + System.currentTimeMillis());

        com.razorpay.Order rzpOrder = razorpayClient.orders.create(req);
        String rzpOrderId = rzpOrder.get("id");

        Order order = new Order();
        order.setRazorpayOrderId(rzpOrderId);
        order.setRegistrationId(registrationId);
        order.setEventId(eventId);
        order.setAmountPaise(amountPaise);
        order.setCurrency("INR");
        order.setStatus(OrderStatus.CREATED);
        order.setCreated_at(LocalDateTime.now());

        ordersRepo.save(order);

        return ResponseEntity.ok(
                Map.of(
                        "keyId", keyId,
                        "razorpayOrderId", rzpOrderId,
                        "amountPaise", amountPaise,
                        "currency", "INR"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) throws Exception {

        String rzpOrderId = body.get("razorpay_order_id");
        String rzpPaymentId = body.get("razorpay_payment_id");
        String rzpSignature = body.get("razorpay_signature");

        if (rzpOrderId == null || rzpPaymentId == null || rzpSignature == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing payment verification fields"));
        }

        String payload = rzpOrderId + "|" + rzpPaymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(
                new SecretKeySpec(
                        keySecret.getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"));

        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String generatedSig = bytesToHex(digest);

        boolean valid = generatedSig.equalsIgnoreCase(rzpSignature);

        Order order = ordersRepo.findByRazorpayOrderId(rzpOrderId)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Order not found: " + rzpOrderId));

        Payment payment = new Payment();
        payment.setRazorpayOrderId(rzpOrderId);
        payment.setRazorpayPaymentId(rzpPaymentId);
        payment.setRegistrationId(order.getRegistrationId());
        payment.setAmountPaise(order.getAmountPaise());
        payment.setCurrency(order.getCurrency());
        payment.setStatus(valid ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setSignatureValid(valid);
        payment.setCreated_at(LocalDateTime.now());

        paymentsRepo.save(payment);

        if (valid) {
            order.setStatus(OrderStatus.PAID);
            ordersRepo.save(order);
        }

        return ResponseEntity.ok(Map.of("signatureValid", valid));
    }

    @PostMapping("/pending")
    public ResponseEntity<?> markPending(@RequestBody Map<String, Object> body) {

        Long registrationId = body.get("registrationId") == null
                ? null
                : Long.valueOf(body.get("registrationId").toString());

        Integer amountRupees = body.get("amountRupees") == null
                ? null
                : Integer.valueOf(body.get("amountRupees").toString());

        if (registrationId == null || amountRupees == null || amountRupees <= 0) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "error",
                            "registrationId and valid amountRupees are required"));
        }

        String suffix = String.valueOf(System.currentTimeMillis());

        Payment payment = new Payment();
        payment.setRegistrationId(registrationId);
        payment.setAmountPaise(amountRupees * 100);
        payment.setCurrency("INR");
        payment.setRazorpayOrderId("PAY_LATER_ORDER_" + suffix);
        payment.setRazorpayPaymentId("PAY_LATER_PAYMENT_" + suffix);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSignatureValid(false);
        payment.setMethod("PAY_LATER");
        payment.setCreated_at(LocalDateTime.now());

        paymentsRepo.save(payment);

        return ResponseEntity.ok(
                Map.of(
                        "status", "PENDING",
                        "registrationId", registrationId));
    }

    @GetMapping("/status/{registrationId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long registrationId) {
        Optional<Payment> latest = paymentsRepo.findTopByRegistrationIdOrderByIdDesc(registrationId);
        if (latest.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No payment found for registrationId: " + registrationId));
        }
        Payment p = latest.get();
        Map<String, Object> response = new HashMap<>();
        response.put("registrationId", registrationId);
        response.put("status", p.getStatus().name());
        response.put("paymentId", p.getId());
        response.put("razorpayOrderId", p.getRazorpayOrderId());
        response.put("razorpayPaymentId", p.getRazorpayPaymentId());
        response.put("amountPaise", p.getAmountPaise());
        response.put("currency", p.getCurrency());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> markCancelled(@RequestBody Map<String, Object> body) {

        Long registrationId = body.get("registrationId") == null
                ? null
                : Long.valueOf(body.get("registrationId").toString());

        String razorpayOrderId = body.get("razorpayOrderId") == null
                ? null
                : body.get("razorpayOrderId").toString();

        String reason = body.get("reason") == null ? "USER_CANCELLED" : body.get("reason").toString();

        if (registrationId == null
                && (razorpayOrderId == null || razorpayOrderId.isBlank())) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "registrationId or razorpayOrderId is required"));
        }

        if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
            ordersRepo.findByRazorpayOrderId(razorpayOrderId)
                    .ifPresent(order -> {
                        order.setStatus(OrderStatus.CANCELLED);
                        ordersRepo.save(order);
                    });

            if (registrationId == null) {
                Optional<Order> orderOpt = ordersRepo.findByRazorpayOrderId(razorpayOrderId);
                if (orderOpt.isPresent()) {
                    registrationId = orderOpt.get().getRegistrationId();
                }
            }
        }

        if (registrationId != null) {
            Optional<Payment> latestPayment = paymentsRepo.findTopByRegistrationIdOrderByIdDesc(registrationId);
            if (latestPayment.isPresent()) {
                Payment latest = latestPayment.get();
                latest.setSignatureValid(false);
                if (latest.getStatus() == PaymentStatus.CREATED) {
                    latest.setStatus(PaymentStatus.FAILED);
                }
                latest.setLastWebhookPayload(
                        "{\"event\":\"CHECKOUT_CANCELLED\",\"reason\":\"" + jsonEscape(reason)
                                + "\",\"time\":\"" + LocalDateTime.now() + "\"}");
                paymentsRepo.save(latest);

                return ResponseEntity.ok(Map.of(
                        "status", latest.getStatus().name(),
                        "registrationId", registrationId,
                        "paymentId", latest.getId(),
                        "updated", true
                ));
            }
        }

        return ResponseEntity.ok(Map.of(
                "status", "CANCELLED",
                "registrationId", registrationId,
                "updated", false
        ));
    }

    private String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}