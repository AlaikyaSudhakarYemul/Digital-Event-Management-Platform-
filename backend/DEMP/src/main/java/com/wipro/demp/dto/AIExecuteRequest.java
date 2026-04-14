package com.wipro.demp.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIExecuteRequest {
    private String prompt;
    private Map<String, Object> context = new HashMap<>();
}
