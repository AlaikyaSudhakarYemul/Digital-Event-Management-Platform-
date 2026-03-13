package com.wipro.demp.repository;



import com.wipro.demp.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
}
