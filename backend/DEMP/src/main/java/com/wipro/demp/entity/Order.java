package com.wipro.demp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(
  name = "orders",
  uniqueConstraints = @UniqueConstraint(name = "uk_orders_rzp_order", columnNames = "razorpayOrderId"),
  indexes = {
    @Index(name = "idx_orders_registration_id", columnList = "registrationId"),
    @Index(name = "idx_orders_event_id", columnList = "eventId")
  }
)

@Getter
@Setter

public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String razorpayOrderId;

  private Long eventId;          // optional, if you want to store event link
  private Long registrationId;   // link to your Registration row

  @Column(nullable = false)
  private Integer amountPaise;

  @Column(nullable = false, length = 10)
  private String currency = "INR";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OrderStatus status = OrderStatus.CREATED;

  private LocalDateTime created_at = LocalDateTime.now();
  private LocalDateTime updated_at;

  @PreUpdate
  public void preUpdate() { this.updated_at = LocalDateTime.now(); }

  // getters & setters
  // ...
  
  
}
