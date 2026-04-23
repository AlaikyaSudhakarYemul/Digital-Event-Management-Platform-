package com.wipro.demp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wipro.demp.constants.DempConstants;
import com.wipro.demp.dto.chatbot.ChatbotRequest;
import com.wipro.demp.dto.chatbot.ChatbotResponse;
import com.wipro.demp.service.chatbot.ChatbotService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping(DempConstants.API_URL + DempConstants.CHATBOT_URL)
@CrossOrigin(origins = DempConstants.FRONTEND_URL)
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatbotResponse> message(@Valid @RequestBody ChatbotRequest request) {
        return ResponseEntity.ok(chatbotService.reply(request));
    }
}
