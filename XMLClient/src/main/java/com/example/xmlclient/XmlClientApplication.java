package com.example.xmlclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@EnableConfigurationProperties(XmlClientProperties.class)
public class XmlClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmlClientApplication.class, args);
    }

    @Bean
    RestClient restClient(RestClient.Builder builder, XmlClientProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
