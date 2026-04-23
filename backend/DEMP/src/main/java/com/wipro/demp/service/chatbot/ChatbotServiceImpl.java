package com.wipro.demp.service.chatbot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wipro.demp.dto.chatbot.ChatbotLink;
import com.wipro.demp.dto.chatbot.ChatbotRequest;
import com.wipro.demp.dto.chatbot.ChatbotResponse;
import com.wipro.demp.entity.Event;
import com.wipro.demp.entity.EventStatus;
import com.wipro.demp.entity.Payment;
import com.wipro.demp.entity.Registrations;
import com.wipro.demp.entity.Ticket;
import com.wipro.demp.repository.EventRepository;
import com.wipro.demp.repository.PaymentsRepository;
import com.wipro.demp.repository.RegistrationRepository;
import com.wipro.demp.repository.TicketRepository;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final TicketRepository ticketRepository;
    private final PaymentsRepository paymentsRepository;

    public ChatbotServiceImpl(EventRepository eventRepository,
                              RegistrationRepository registrationRepository,
                              TicketRepository ticketRepository,
                              PaymentsRepository paymentsRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.ticketRepository = ticketRepository;
        this.paymentsRepository = paymentsRepository;
    }

    @Override
    public ChatbotResponse reply(ChatbotRequest request) {
        String text = request.getMessage() == null ? "" : request.getMessage().trim();
        String lower = text.toLowerCase(Locale.ROOT);

        if (lower.isBlank()) {
            return new ChatbotResponse(
                    "Please type a message so I can help you.",
                    "EMPTY",
                    List.of("Show upcoming events", "How to register", "How to buy tickets"),
                    List.of(
                        homeLink("Browse Home"),
                        dashboardLink("My Dashboard")
                    )
            );
        }

        if (containsAny(lower, "my registration", "my registrations", "registration status", "registered events")) {
            return buildRegistrationStatusResponse(request.getUserId());
        }

        if (containsAny(lower, "ticket status", "my tickets", "ticket details")) {
            return buildTicketStatusResponse(request.getUserId());
        }

        if (containsAny(lower, "event", "upcoming", "show events", "list events")) {
            return buildUpcomingEventsResponse();
        }

        if (containsAny(lower, "register", "registration", "join event")) {
            return new ChatbotResponse(
                    "To register: open an event, click Register, then proceed to payment or generate tickets if eligible.",
                    "REGISTRATION_HELP",
                    List.of("Show upcoming events", "View my registrations", "How to buy tickets"),
                    List.of(
                        homeLink("Browse Events"),
                        dashboardLink("Go To Dashboard")
                    )
            );
        }

        if (containsAny(lower, "ticket", "tickets", "generate ticket", "buy ticket")) {
            return new ChatbotResponse(
                    "To generate tickets: register for the event first, click Generate Ticket, choose type and quantity, then submit.",
                    "TICKET_HELP",
                    List.of("View my tickets", "Ticket limits", "Payment help"),
                    List.of(
                        dashboardLink("Go To Dashboard"),
                        paymentsLink("Open Payments", null, null, 499)
                    )
            );
        }

        if (containsAny(lower, "payment", "pay", "razorpay", "order")) {
            return new ChatbotResponse(
                    "For payment issues, verify you are logged in and complete payment from the Payments page before ticket generation.",
                    "PAYMENT_HELP",
                    List.of("Ticket status", "Retry payment", "Check pending payment"),
                    List.of(
                        paymentsLink("Open Payments", null, null, 499),
                        dashboardLink("Go To Dashboard")
                    )
            );
        }

        if (containsAny(lower, "organizer", "create event", "dashboard")) {
            return new ChatbotResponse(
                    "Organizers can create and manage events from Organizer Dashboard once logged in with organizer role.",
                    "ORGANIZER_HELP",
                    List.of("Create event steps", "View registrations", "Manage event speakers"),
                    List.of(link("Open Organizer Dashboard", "/organizer/dashboard", "dashboard", Map.of()))
            );
        }

        return new ChatbotResponse(
                "I can help with events, registration, tickets, payments, and organizer dashboard actions.",
                "GENERAL",
                List.of("Show upcoming events", "How to register", "How to buy tickets"),
                List.of(
                    homeLink("Browse Home"),
                    dashboardLink("Go To Dashboard")
                )
        );
    }

            private ChatbotResponse buildUpcomingEventsResponse() {
            LocalDate today = LocalDate.now();

            List<Event> upcoming = eventRepository.findAll().stream()
                .filter(event -> !event.isDeleted())
                .filter(event -> event.getDate() != null && !event.getDate().isBefore(today))
                .filter(event -> event.getActiveStatus() == EventStatus.ACTIVE)
                .sorted(Comparator.comparing(Event::getDate).thenComparing(Event::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(5)
                .toList();

            if (upcoming.isEmpty()) {
                return new ChatbotResponse(
                    "No active upcoming events are available right now.",
                    "EVENT_DISCOVERY",
                    List.of("How to register", "Organizer dashboard help"),
                    List.of(homeLink("Browse Home"))
                );
            }

            String items = upcoming.stream()
                .map(event -> {
                    String dateText = event.getDate() == null ? "date TBD" : event.getDate().format(DATE_FORMAT);
                    String timeText = event.getTime() == null ? "" : " at " + event.getTime();
                    return event.getEventName() + " on " + dateText + timeText;
                })
                .collect(Collectors.joining("; "));

            String reply = "Upcoming events: " + items + ".";

            List<ChatbotLink> links = upcoming.stream()
                .limit(3)
                .map(event -> eventLink("View Event #" + event.getEventId() + ": " + event.getEventName(), event.getEventId()))
                .collect(Collectors.toList());
            links.add(homeLink("Browse All Events"));

            return new ChatbotResponse(
                reply,
                "EVENT_DISCOVERY",
                List.of("How to register for an event", "How to buy tickets", "View my registrations"),
                links
            );
            }

            private ChatbotResponse buildRegistrationStatusResponse(Integer userId) {
            if (userId == null || userId <= 0) {
                return new ChatbotResponse(
                    "Please login first so I can fetch your registrations.",
                    "REGISTRATION_STATUS",
                    List.of("How to register", "Show upcoming events"),
                    List.of(homeLink("Go To Login/Home"))
                );
            }

            List<Registrations> registrations = registrationRepository.findByUserUserId(userId).stream()
                .filter(reg -> !reg.isDeleted())
                .sorted(Comparator.comparing(Registrations::getRegistrationId).reversed())
                .toList();

            if (registrations.isEmpty()) {
                return new ChatbotResponse(
                    "You do not have any registrations yet.",
                    "REGISTRATION_STATUS",
                    List.of("Show upcoming events", "How to register"),
                    List.of(homeLink("Browse Events"))
                );
            }

            Map<String, Long> statusCounts = registrations.stream()
                .collect(Collectors.groupingBy(reg -> reg.getStatus() == null ? "UNKNOWN" : reg.getStatus().name(), Collectors.counting()));

            String statusSummary = statusCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

            List<String> recentEvents = registrations.stream()
                .limit(3)
                .map(reg -> reg.getEvent() == null ? "Event" : reg.getEvent().getEventName())
                .toList();

            String reply = "You have " + registrations.size() + " registration(s). Status summary - " + statusSummary
                + ". Recent events: " + String.join(", ", recentEvents) + ".";

            List<ChatbotLink> links = registrations.stream()
                .limit(3)
                .map(reg -> {
                    int eventId = reg.getEvent() == null ? 0 : reg.getEvent().getEventId();
                    String eventName = reg.getEvent() == null ? "Event" : reg.getEvent().getEventName();
                    if (eventId > 0) {
                        return registrationEventLink("Registration #" + reg.getRegistrationId() + " - " + eventName,
                                reg.getRegistrationId(), eventId);
                    }
                    return dashboardLink("Registration #" + reg.getRegistrationId() + " - Dashboard");
                })
                .collect(Collectors.toList());

            Registrations latest = registrations.get(0);
            Integer latestRegId = latest.getRegistrationId();
            Integer latestEventId = latest.getEvent() == null ? null : latest.getEvent().getEventId();

            links.add(dashboardLink("My Dashboard"));
            links.add(paymentsLink("Payments", latestRegId, latestEventId, 499));

            return new ChatbotResponse(
                reply,
                "REGISTRATION_STATUS",
                List.of("How to buy tickets", "View my tickets", "Payment help"),
                links
            );
            }

            private ChatbotResponse buildTicketStatusResponse(Integer userId) {
            if (userId == null || userId <= 0) {
                return new ChatbotResponse(
                    "Please login first so I can fetch your ticket status.",
                    "TICKET_STATUS",
                    List.of("How to buy tickets", "How to register"),
                    List.of(homeLink("Go To Login/Home"))
                );
            }

            List<Ticket> tickets = ticketRepository.findByUserId(userId).stream()
                .filter(ticket -> !ticket.isDeleted())
                .sorted(Comparator.comparing(Ticket::getTicketId).reversed())
                .toList();

            if (tickets.isEmpty()) {
                return new ChatbotResponse(
                    "You do not have any tickets yet.",
                    "TICKET_STATUS",
                    List.of("How to register", "How to buy tickets", "Show upcoming events"),
                    List.of(homeLink("Browse Events"), dashboardLink("My Dashboard"))
                );
            }

            int totalQuantity = tickets.stream().mapToInt(Ticket::getQuantity).sum();
            BigDecimal totalAmount = tickets.stream()
                .map(ticket -> ticket.getTotalAmount() == null ? ticket.getPrice() : ticket.getTotalAmount())
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Integer> paymentCounts = new HashMap<>();
            paymentCounts.put("SUCCESS", 0);
            paymentCounts.put("PENDING", 0);
            paymentCounts.put("FAILED", 0);
            paymentCounts.put("UNKNOWN", 0);

            for (Ticket ticket : tickets) {
                String key = resolvePaymentStatusKey(ticket.getRegistrationId());
                paymentCounts.put(key, paymentCounts.getOrDefault(key, 0) + 1);
            }

            List<String> recentTicketIds = tickets.stream()
                .limit(3)
                .map(ticket -> String.valueOf(ticket.getTicketId()))
                .toList();

            String reply = "You have " + tickets.size() + " ticket row(s), total quantity " + totalQuantity
                + ", estimated amount " + totalAmount + ". Payment status - SUCCESS: " + paymentCounts.get("SUCCESS")
                + ", PENDING: " + paymentCounts.get("PENDING") + ", FAILED: " + paymentCounts.get("FAILED")
                + ". Recent ticket IDs: " + String.join(", ", recentTicketIds) + ".";

            List<ChatbotLink> links = tickets.stream()
                .map(Ticket::getEventId)
                .filter(eventId -> eventId > 0)
                .distinct()
                .limit(3)
                .map(eventId -> eventLink("Open Event #" + eventId, eventId))
                .collect(Collectors.toList());

            Ticket latestTicket = tickets.get(0);
            links.add(dashboardLink("My Dashboard"));
            links.add(paymentsLink("Payments", latestTicket.getRegistrationId(), latestTicket.getEventId(), 499));

            return new ChatbotResponse(
                reply,
                "TICKET_STATUS",
                List.of("Payment help", "View my registrations", "Show upcoming events"),
                links
            );
            }

            private String resolvePaymentStatusKey(int registrationId) {
            if (registrationId <= 0) {
                return "UNKNOWN";
            }

            Payment payment = paymentsRepository.findTopByRegistrationIdOrderByIdDesc((long) registrationId).orElse(null);
            if (payment == null || payment.getStatus() == null) {
                return "UNKNOWN";
            }

            return switch (payment.getStatus()) {
                case SUCCESS -> "SUCCESS";
                case PENDING, CREATED -> "PENDING";
                case FAILED, REFUNDED -> "FAILED";
            };
            }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private ChatbotLink link(String label, String url, String routeType, Map<String, Object> meta) {
        return new ChatbotLink(label, url, routeType, meta == null ? Map.of() : meta);
    }

    private ChatbotLink homeLink(String label) {
        return link(label, "/", "home", Map.of());
    }

    private ChatbotLink dashboardLink(String label) {
        return link(label, "/userdashboard", "dashboard", Map.of());
    }

    private ChatbotLink eventLink(String label, int eventId) {
        return link(label, "/events/" + eventId, "event", Map.of("eventId", eventId));
    }

    private ChatbotLink registrationEventLink(String label, int registrationId, int eventId) {
        return link(label, "/events/" + eventId, "event", Map.of(
                "registrationId", registrationId,
                "eventId", eventId
        ));
    }

    private ChatbotLink paymentsLink(String label, Integer registrationId, Integer eventId, Integer amountRupees) {
        Map<String, Object> meta = new HashMap<>();
        if (registrationId != null && registrationId > 0) {
            meta.put("registrationId", registrationId);
        }
        if (eventId != null && eventId > 0) {
            meta.put("eventId", eventId);
        }
        meta.put("amountRupees", amountRupees == null || amountRupees <= 0 ? 499 : amountRupees);
        return link(label, "/payments", "payments", meta);
    }

}
