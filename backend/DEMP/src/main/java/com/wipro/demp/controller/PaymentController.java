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
import java.util.Base64;
import java.util.Map;

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

    // Generate signature HMAC_SHA256(orderId|paymentId)
    String payload = rzpOrderId + "|" + rzpPaymentId;
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    String generatedSig = Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));

    boolean valid = generatedSig.equals(rzpSignature);

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
}	
