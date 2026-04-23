package com.wipro.demp.service.chatbot;

import com.wipro.demp.dto.chatbot.ChatbotRequest;
import com.wipro.demp.dto.chatbot.ChatbotResponse;

public interface ChatbotService {

    ChatbotResponse reply(ChatbotRequest request);
}
