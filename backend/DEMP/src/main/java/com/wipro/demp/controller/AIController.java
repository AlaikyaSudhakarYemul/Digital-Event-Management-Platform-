package com.wipro.demp.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.wipro.demp.config.JwtUtil;
import com.wipro.demp.constants.DempConstants;
import com.wipro.demp.dto.AIExecuteRequest;
import com.wipro.demp.entity.Speaker;

@RestController
@RequestMapping(DempConstants.API_URL + DempConstants.AI_URL)
public class AIController {

    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;

    public AIController(RestTemplate restTemplate, JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/execute")
    public ResponseEntity<?> execute(@RequestBody AIExecuteRequest request,
                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "prompt is required"));
        }

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid token"));
        }

        String role;
        try {
            role = jwtUtil.extractRole(authorization);
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        if (role == null || !"ORGANIZER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only organizer can use AI create workflow"));
        }

        Map<String, Object> context = request.getContext() == null ? Map.of() : request.getContext();
        String prompt = request.getPrompt();

        String intent = (prompt.toLowerCase().contains("create") && prompt.toLowerCase().contains("event"))
                ? "create_event" : "unknown";

        if (!"create_event".equals(intent)) {
            return ResponseEntity.ok(Map.of(
                    "status", "unsupported",
                    "message", "Only create_event prompt is supported right now"
            ));
        }

        String eventName = extractEventName(prompt);
        String eventType = extractEventType(prompt);
        Integer speakerCount = extractNumber(prompt, "(\\d+)\\s+speakers?");
        Integer maxAttendees = extractNumber(prompt, "(?:max|capacity|attendees?)\\s*(\\d+)");
        Integer price = extractNumber(prompt, "(?:price|rs|₹)\\s*(\\d+)");

        Integer userId = toInt(context.get("userId"));
        Integer addressId = toInt(context.get("addressId"));
        String date = toStringOrDefault(context.get("defaultDate"), LocalDate.now().plusDays(7).toString());
        String time = toStringOrDefault(context.get("defaultTime"), "18:00:00");

        List<String> missingFields = new ArrayList<>();
        if (userId == null) {
            missingFields.add("userId");
        }
        if (("IN_PERSON".equals(eventType) || "HYBRID".equals(eventType)) && addressId == null) {
            missingFields.add("addressId");
        }

        List<String> unsupportedFields = new ArrayList<>();
        if (price != null) {
            unsupportedFields.add("price");
        }

        Map<String, Object> command = new HashMap<>();
        command.put("intent", intent);
        command.put("confidence", 0.9);
        command.put("missingFields", missingFields);
        command.put("unsupportedFields", unsupportedFields);

        if (!missingFields.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "status", "needs_input",
                    "message", "Missing required information to create event",
                    "command", command,
                    "missingFields", missingFields,
                    "unsupportedFields", unsupportedFields
            ));
        }

        List<Integer> speakerIds = fetchSpeakerIds(speakerCount == null ? 0 : speakerCount, authorization);

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("eventName", eventName);
        eventPayload.put("description", "Created by AI assistant from prompt: " + prompt);
        eventPayload.put("eventType", eventType);
        eventPayload.put("date", date);
        eventPayload.put("time", time);
        eventPayload.put("addressId", addressId);
        eventPayload.put("speakerIds", speakerIds);
        eventPayload.put("userId", userId);
        eventPayload.put("maxAttendees", maxAttendees == null ? 100 : maxAttendees);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorization);

        try {
                ResponseEntity<Map<String, Object>> created = restTemplate.exchange(
                    "http://EVENT/api/events/create",
                    HttpMethod.POST,
                    new HttpEntity<>(eventPayload, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

                Map<String, Object> body = created.getBody() == null ? Map.of() : created.getBody();
            return ResponseEntity.ok(Map.of(
                    "status", "created",
                    "message", "Event created successfully",
                    "eventId", body.get("eventId"),
                    "summary", Map.of(
                            "eventName", body.get("eventName"),
                            "eventType", body.get("eventType"),
                            "speakerIds", speakerIds
                    ),
                    "unsupportedFields", unsupportedFields
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(502).body(Map.of(
                    "status", "failed",
                    "message", "Failed to orchestrate event creation",
                    "details", ex.getMessage()
            ));
        }
    }

    private String extractEventName(String prompt) {
        Matcher quoted = Pattern.compile("\"([^\"]+)\"").matcher(prompt);
        if (quoted.find()) {
            return quoted.group(1);
        }
        if (prompt.toLowerCase().contains("technical")) {
            return "Technical Event";
        }
        return "AI Generated Event";
    }

    private String extractEventType(String prompt) {
        String p = prompt.toLowerCase();
        if (p.contains("online") || p.contains("virtual")) {
            return "VIRTUAL";
        }
        if (p.contains("hybrid")) {
            return "HYBRID";
        }
        if (p.contains("offline") || p.contains("in-person") || p.contains("in person")) {
            return "IN_PERSON";
        }
        return "VIRTUAL";
    }

    private Integer extractNumber(String prompt, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(prompt);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Exception ex) {
            return null;
        }
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception ex) {
            return null;
        }
    }

    private String toStringOrDefault(Object value, String fallback) {
        if (value == null || value.toString().isBlank()) {
            return fallback;
        }
        return value.toString();
    }

    private List<Integer> fetchSpeakerIds(int count, String authorization) {
        if (count <= 0) {
            return List.of();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        ResponseEntity<Speaker[]> response = restTemplate.exchange(
                "http://ADMIN/api/speakers",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Speaker[].class
        );

        Speaker[] speakers = response.getBody();
        if (speakers == null || speakers.length == 0) {
            return List.of();
        }

        List<Integer> ids = new ArrayList<>();
        for (Speaker speaker : speakers) {
            if (speaker != null) {
                ids.add(speaker.getSpeakerId());
            }
            if (ids.size() >= count) {
                break;
            }
        }
        return ids;
    }
}
