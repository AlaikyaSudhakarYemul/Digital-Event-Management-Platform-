package com.wipro.tickets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.wipro.tickets.tickets.TicketsApplication;

@SpringBootTest(classes = TicketsApplication.class)
@ActiveProfiles("test")
class TicketsApplicationTests {

    @Test
    void contextLoads() {
    }
}
