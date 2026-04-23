package com.wipro.demp.dto.chatbot;

import jakarta.validation.constraints.NotBlank;

public class ChatbotRequest {

    @NotBlank(message = "message is required")
    private String message;

    private Integer userId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
