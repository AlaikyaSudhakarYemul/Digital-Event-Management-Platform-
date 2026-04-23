package com.wipro.demp.dto.chatbot;

import java.util.Map;

public class ChatbotLink {

    private String label;
    private String url;
    private String routeType;
    private Map<String, Object> meta;

    public ChatbotLink() {
    }

    public ChatbotLink(String label, String url) {
        this.label = label;
        this.url = url;
    }

    public ChatbotLink(String label, String url, String routeType) {
        this.label = label;
        this.url = url;
        this.routeType = routeType;
    }

    public ChatbotLink(String label, String url, String routeType, Map<String, Object> meta) {
        this.label = label;
        this.url = url;
        this.routeType = routeType;
        this.meta = meta;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
}
