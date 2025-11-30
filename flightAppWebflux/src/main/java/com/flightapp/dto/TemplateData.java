package com.flightapp.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TemplateData {
    private Map<String, Object> vars;
}
