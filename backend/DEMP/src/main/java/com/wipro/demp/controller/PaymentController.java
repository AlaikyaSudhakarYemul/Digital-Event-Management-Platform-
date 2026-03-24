package com.wipro.demp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.razorpay.RazorpayClient;
import com.wipro.demp.entity.Order;
import com.wipro.demp.entity.OrderStatus;
import com.wipro.demp.entity.Payment;
import com.wipro.demp.entity.PaymentStatus;
import com.wipro.demp.repository.OrdersRepository;
import com.wipro.demp.repository.PaymentsRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins="http://localhost:3000")
public class PaymentController {

  private final RazorpayClient razorpayClient;
  private final OrdersRepository ordersRepo;
  private final PaymentsRepository paymentsRepo;

  @Value("${razorpay.keyId}")
  private String keyId;

  @Value("${razorpay.keySecret}")
  private String keySecret;

  public PaymentController(RazorpayClient razorpayClient, OrdersRepository ordersRepo, PaymentsRepository paymentsRepo) {
    this.razorpayClient = razorpayClient;
    this.ordersRepo = ordersRepo;
    this.paymentsRepo = paymentsRepo;
  }

  // Create Razorpay Order and persist in orders table
  @PostMapping("/orders")
  public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) throws Exception {
    // Expect body to contain either registrationId or eventId; you decide your domain
    Long registrationId = body.get("registrationId") == null ? null : Long.valueOf(body.get("registrationId").toString());
    Long eventId        = body.get("eventId") == null ? null : Long.valueOf(body.get("eventId").toString());

    // TODO: compute amount from your DB logic (Registration -> Event -> priceRupees)
    // For now, expect amountRupees from the request (for dev/testing)
    Integer amountRupees = Integer.valueOf(body.get("amountRupees").toString());
    int amountPaise = amountRupees * 100;

    // Create order in Razorpay
    org.json.JSONObject req = new org.json.JSONObject();
    req.put("amount", amountPaise);
    req.put("currency", "INR");
    req.put("receipt", "rcpt_" + System.currentTimeMillis());

    com.razorpay.Order rzpOrder = razorpayClient.orders.create(req);
    String rzpOrderId = rzpOrder.get("id");

    // Persist in DB
    Order order = new Order();
    order.setRazorpayOrderId(rzpOrderId);
    order.setRegistrationId(registrationId);
    order.setEventId(eventId);
    order.setAmountPaise(amountPaise);
    order.setCurrency("INR");
    order.setStatus(OrderStatus.CREATED);
    order.setCreated_at(LocalDateTime.now());
    ordersRepo.save(order);

    // Return data needed by frontend to open Checkout
    return ResponseEntity.ok(Map.of(
      "keyId", keyId,
      "razorpayOrderId", rzpOrderId,
      "amountPaise", amountPaise,
      "currency", "INR"
    ));
  }

  // Verify signature and persist payment in payments table
  @PostMapping("/verify")
  public ResponseEntity<?> verify(@RequestBody Map<String, String> body) throws Exception {
    String rzpOrderId   = body.get("razorpay_order_id");
    String rzpPaymentId = body.get("razorpay_payment_id");
    String rzpSignature = body.get("razorpay_signature");

    if (rzpOrderId == null || rzpPaymentId == null || rzpSignature == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Missing payment verification fields"));
    }

    // Generate signature HMAC_SHA256(orderId|paymentId)
    String payload = rzpOrderId + "|" + rzpPaymentId;
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    String generatedSig = bytesToHex(digest);

    boolean valid = generatedSig.equalsIgnoreCase(rzpSignature);

    // Load order row to know amount and registration link
    Order order = ordersRepo.findByRazorpayOrderId(rzpOrderId)
        .orElseThrow(() -> new IllegalStateException("Order not found: " + rzpOrderId));

    // Save payment attempt
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

      // TODO: mark your Registration as PAID in your domain (e.g., registrationService.markPaid(order.getRegistrationId()))
    }

    return ResponseEntity.ok(Map.of("signatureValid", valid));
  }

  // Persist pending payment row when user chooses pay later
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

  // Update DB when user closes Razorpay checkout or payment fails on client side
  @PostMapping("/cancel")
  public ResponseEntity<?> markCancelled(@RequestBody Map<String, Object> body) {
    Long registrationId = body.get("registrationId") == null ? null : Long.valueOf(body.get("registrationId").toString());
    String razorpayOrderId = body.get("razorpayOrderId") == null ? null : body.get("razorpayOrderId").toString();
    String reason = body.get("reason") == null ? "USER_CANCELLED" : body.get("reason").toString();

    if (registrationId == null && (razorpayOrderId == null || razorpayOrderId.isBlank())) {
      return ResponseEntity.badRequest().body(Map.of("error", "registrationId or razorpayOrderId is required"));
    }

    if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
      ordersRepo.findByRazorpayOrderId(razorpayOrderId).ifPresent(order -> {
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
        latest.setLastWebhookPayload("{\"event\":\"CHECKOUT_CANCELLED\",\"reason\":\"" + jsonEscape(reason) + "\",\"time\":\"" + LocalDateTime.now() + "\"}");
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
    StringBuilder hex = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }
}	
