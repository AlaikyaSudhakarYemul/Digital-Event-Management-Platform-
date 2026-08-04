package com.wipro.demp.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.wipro.demp.controller.PaymentController;
import com.wipro.demp.entity.Order;
import com.wipro.demp.entity.OrderStatus;
import com.wipro.demp.entity.Payment;
import com.wipro.demp.entity.PaymentStatus;
import com.wipro.demp.repository.OrdersRepository;
import com.wipro.demp.repository.PaymentsRepository;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    public static class FakeRazorpayClient {
        public final FakeOrders orders = new FakeOrders();
    }

    public static class FakeOrders {
        public FakeOrder create(org.json.JSONObject req) {
            return new FakeOrder("order_test_123");
        }
    }

    public static class FakeOrder {
        private final String id;

        public FakeOrder(String id) {
            this.id = id;
        }

        public Object get(String key) {
            return "id".equals(key) ? id : null;
        }
    }

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private PaymentsRepository paymentsRepository;

    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        paymentController = new PaymentController(new FakeRazorpayClient(), ordersRepository, paymentsRepository);
        ReflectionTestUtils.setField(paymentController, "keyId", "rzp_test_key");
        ReflectionTestUtils.setField(paymentController, "keySecret", "test_secret_key");
    }

    @Test
    void createOrderSuccess() throws Exception {
        when(ordersRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> payload = new HashMap<>();
        payload.put("registrationId", 10);
        payload.put("eventId", 20);
        payload.put("amountRupees", 499);

        ResponseEntity<?> response = paymentController.createOrder(payload);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("rzp_test_key", body.get("keyId"));
        assertEquals("order_test_123", body.get("razorpayOrderId"));
        assertEquals(49900, body.get("amountPaise"));
    }

    @Test
    void verifyMissingFieldsReturnsBadRequest() throws Exception {
        ResponseEntity<?> response = paymentController.verify(Map.of());

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void markPendingBadRequestWhenAmountInvalid() {
        ResponseEntity<?> response = paymentController.markPending(Map.of("registrationId", 11, "amountRupees", 0));

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void markCancelledUpdatesLatestPayment() {
        Order order = new Order();
        order.setRegistrationId(22L);
        order.setStatus(OrderStatus.CREATED);
        when(ordersRepository.findByRazorpayOrderId("order_abc")).thenReturn(Optional.of(order));

        Payment latest = new Payment();
        latest.setId(777L);
        latest.setStatus(PaymentStatus.CREATED);
        when(paymentsRepository.findTopByRegistrationIdOrderByIdDesc(22L)).thenReturn(Optional.of(latest));

        ResponseEntity<?> response = paymentController.markCancelled(Map.of("razorpayOrderId", "order_abc", "reason", "checkout_closed"));

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("updated"));
        assertEquals("FAILED", body.get("status"));

        verify(ordersRepository).save(any(Order.class));
        verify(paymentsRepository).save(any(Payment.class));
    }
}
