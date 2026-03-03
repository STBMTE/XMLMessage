package com.example.xmlclient;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xml.client")
public record XmlClientProperties(
        String baseUrl,
        String path
) {
}
