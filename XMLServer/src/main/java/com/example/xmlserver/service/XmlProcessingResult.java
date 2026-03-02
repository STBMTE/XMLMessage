package com.example.xmlserver.service;

import org.springframework.http.HttpStatus;

public record XmlProcessingResult(HttpStatus httpStatus, String responseXml) {
}
