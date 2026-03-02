package com.example.xmlserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "messages")
public class XmlMessage {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time", nullable = false)
    private LocalTime time;

    // \" Для избежания ошибки использования зарезервированного имени вместо параметра в запросе
    @Column(name = "\"user\"", nullable = false, columnDefinition = "TEXT")
    private String user;

    // \" Для избежания ошибки использования зарезервированного имени вместо параметра в запросе
    @Column(name = "\"text\"", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "result", nullable = false)
    private Integer result;

    public XmlMessage(
            LocalTime time,
            String user,
            String text,
            Integer result
    ) {
        this.time = time;
        this.user = user;
        this.text = text;
        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public Integer getResult() {
        return result;
    }

    public static XmlMessage createForMessage(LocalTime time, String user, String text, int code) {
        return new XmlMessage(
                time,
                user,
                text,
                code
        );
    }
}
