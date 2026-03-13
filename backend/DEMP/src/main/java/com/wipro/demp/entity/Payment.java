package com.wipro.demp.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
  name = "payments",
  uniqueConstraints = @UniqueConstraint(name = "uk_payments_rzp_payment", columnNames = "razorpayPaymentId"),
  indexes = {
    @Index(name = "idx_payments_rzp_order", columnList = "razorpayOrderId"),
    @Index(name = "idx_payments_registration_id", columnList = "registrationId")
  }
)

@Getter
@Setter
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String razorpayPaymentId;

  @Column(nullable = false, length = 255)
  private String razorpayOrderId;

  private Long registrationId;

  @Column(nullable = false)
  private Integer amountPaise;

  @Column(nullable = false, length = 10)
  private String currency = "INR";

  @Column(length = 50)
  private String method;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status = PaymentStatus.CREATED;

  private Integer fee;
  private Integer tax;

  @Column(nullable = false)
  private Boolean signatureValid = false;

  @Column(columnDefinition = "JSON")
  private String lastWebhookPayload;  // store as JSON in MySQL 8.4

  private LocalDateTime created_at = LocalDateTime.now();
  private LocalDateTime updated_at;

  @PreUpdate
  public void preUpdate() { this.updated_at = LocalDateTime.now(); }

  // getters & setters
  // ...
  
  
}
