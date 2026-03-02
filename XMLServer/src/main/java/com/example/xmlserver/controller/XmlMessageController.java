package com.example.xmlserver.controller;

import com.example.xmlserver.service.XmlMessageService;
import com.example.xmlserver.service.XmlProcessingResult;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/xml")
public class XmlMessageController {

    private final XmlMessageService xmlMessageService;

    public XmlMessageController(XmlMessageService xmlMessageService) {
        this.xmlMessageService = xmlMessageService;
    }

    @PostMapping(
            value = "/message",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<@NonNull String> validateAndSave(@RequestBody String requestXml) {
        XmlProcessingResult result = xmlMessageService.validateAndSave(requestXml);
        return ResponseEntity.status(result.httpStatus())
                .contentType(MediaType.APPLICATION_XML)
                .body(result.responseXml());
    }
}
