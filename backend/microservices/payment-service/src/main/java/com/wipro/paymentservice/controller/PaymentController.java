package com.wipro.paymentservice.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.razorpay.RazorpayClient;
import com.wipro.paymentservice.entity.Order;
import com.wipro.paymentservice.entity.OrderStatus;
import com.wipro.paymentservice.entity.Payment;
import com.wipro.paymentservice.entity.PaymentStatus;
import com.wipro.paymentservice.repository.OrdersRepository;
import com.wipro.paymentservice.repository.PaymentsRepository;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    private final RazorpayClient razorpayClient;
    private final OrdersRepository ordersRepo;
    private final PaymentsRepository paymentsRepo;

    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    public PaymentController(RazorpayClient razorpayClient,
                             OrdersRepository ordersRepo,
                             PaymentsRepository paymentsRepo) {
        this.razorpayClient = razorpayClient;
        this.ordersRepo = ordersRepo;
        this.paymentsRepo = paymentsRepo;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) throws Exception {
        Long registrationId = body.get("registrationId") == null ? null : Long.valueOf(body.get("registrationId").toString());
        Long eventId = body.get("eventId") == null ? null : Long.valueOf(body.get("eventId").toString());
        Integer amountRupees = Integer.valueOf(body.get("amountRupees").toString());
        int amountPaise = amountRupees * 100;

        org.json.JSONObject req = new org.json.JSONObject();
        req.put("amount", amountPaise);
        req.put("currency", "INR");
        req.put("receipt", "rcpt_" + System.currentTimeMillis());

        org.json.JSONObject rzpOrder = razorpayClient.orders.create(req);
        String rzpOrderId = rzpOrder.getString("id");

        Order order = new Order();
        order.setRazorpayOrderId(rzpOrderId);
        order.setRegistrationId(registrationId);
        order.setEventId(eventId);
        order.setAmountPaise(amountPaise);
        order.setCurrency("INR");
        order.setStatus(OrderStatus.CREATED);
        order.setCreated_at(LocalDateTime.now());
        ordersRepo.save(order);

        return ResponseEntity.ok(Map.of(
                "keyId", keyId,
                "razorpayOrderId", rzpOrderId,
                "amountPaise", amountPaise,
                "currency", "INR"
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) throws Exception {
        String rzpOrderId = body.get("razorpay_order_id");
        String rzpPaymentId = body.get("razorpay_payment_id");
        String rzpSignature = body.get("razorpay_signature");

        if (rzpOrderId == null || rzpPaymentId == null || rzpSignature == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing payment verification fields"));
        }

        String payload = rzpOrderId + "|" + rzpPaymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String generatedSig = bytesToHex(digest);
        boolean valid = generatedSig.equalsIgnoreCase(rzpSignature);

        Order order = ordersRepo.findByRazorpayOrderId(rzpOrderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + rzpOrderId));

        Payment p = new Payment();
        p.setRazorpayOrderId(rzpOrderId);
        p.setRazorpayPaymentId(rzpPaymentId);
        p.setRegistrationId(order.getRegistrationId());
        p.setAmountPaise(order.getAmountPaise());
        p.setCurrency(order.getCurrency());
        p.setStatus(valid ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        p.setSignatureValid(valid);
        p.setCreated_at(LocalDateTime.now());
        paymentsRepo.save(p);

        if (valid) {
            order.setStatus(OrderStatus.PAID);
            ordersRepo.save(order);
        }

        return ResponseEntity.ok(Map.of("signatureValid", valid));
    }

    @PostMapping("/pending")
    public ResponseEntity<?> markPending(@RequestBody Map<String, Object> body) {
        Long registrationId = body.get("registrationId") == null ? null : Long.valueOf(body.get("registrationId").toString());
        Integer amountRupees = body.get("amountRupees") == null ? null : Integer.valueOf(body.get("amountRupees").toString());

        if (registrationId == null || amountRupees == null || amountRupees <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "registrationId and valid amountRupees are required"));
        }

        String suffix = String.valueOf(System.currentTimeMillis());
        Payment p = new Payment();
        p.setRegistrationId(registrationId);
        p.setAmountPaise(amountRupees * 100);
        p.setCurrency("INR");
        p.setRazorpayOrderId("PAY_LATER_ORDER_" + suffix);
        p.setRazorpayPaymentId("PAY_LATER_PAYMENT_" + suffix);
        p.setStatus(PaymentStatus.PENDING);
        p.setSignatureValid(false);
        p.setMethod("PAY_LATER");
        p.setCreated_at(LocalDateTime.now());
        paymentsRepo.save(p);

        return ResponseEntity.ok(Map.of("status", "PENDING", "registrationId", registrationId));
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> markCancelled(@RequestBody Map<String, Object> body) {
        Long registrationId = body.get("registrationId") == null ? null : Long.valueOf(body.get("registrationId").toString());
        String razorpayOrderId = body.get("razorpayOrderId") == null ? null : body.get("razorpayOrderId").toString();

        if (registrationId == null && (razorpayOrderId == null || razorpayOrderId.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "registrationId or razorpayOrderId is required"));
        }

        if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
            ordersRepo.findByRazorpayOrderId(razorpayOrderId).ifPresent(order -> {
                order.setStatus(OrderStatus.CANCELLED);
                ordersRepo.save(order);
            });
        }

        return ResponseEntity.ok(Map.of("status", "CANCELLED"));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
