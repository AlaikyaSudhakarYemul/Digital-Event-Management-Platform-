package com.wipro.demp.dto.chatbot;

import java.util.List;

public class ChatbotResponse {

    private String reply;
    private String intent;
    private List<String> suggestions;
    private List<ChatbotLink> links;

    public ChatbotResponse() {
    }

    public ChatbotResponse(String reply, String intent, List<String> suggestions) {
        this.reply = reply;
        this.intent = intent;
        this.suggestions = suggestions;
    }

    public ChatbotResponse(String reply, String intent, List<String> suggestions, List<ChatbotLink> links) {
        this.reply = reply;
        this.intent = intent;
        this.suggestions = suggestions;
        this.links = links;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<ChatbotLink> getLinks() {
        return links;
    }

    public void setLinks(List<ChatbotLink> links) {
        this.links = links;
    }
}
