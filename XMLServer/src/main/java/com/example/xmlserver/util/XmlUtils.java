package com.example.xmlserver.util;

import com.example.message.MessageDocument;
import com.example.message.MessageType;
import com.example.message.ResponseType;
import com.example.message.StatusType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import java.util.Calendar;
import java.util.TimeZone;

public class XmlUtils {

    private static final XmlOptions PRETTY_XML = new XmlOptions()
            .setSavePrettyPrint()
            .setSaveAggressiveNamespaces();

    private XmlUtils() {
    }

    public static MessageType parseRequest(String requestXml) throws XmlException {
        var a = MessageDocument.Factory.parse(requestXml);
        return a.getMessage();
    }

    public static void validateMessageStructure(MessageType message) {
        if (message == null || message.getHeader() == null || !message.isSetRequest()) {
            throw new IllegalArgumentException("message/header/request are required");
        }
        if (message.getHeader().getTime() == null) {
            throw new IllegalArgumentException("header@time is required");
        }
        if (message.getRequest().getUser() == null || message.getRequest().getUser().isBlank()) {
            throw new IllegalArgumentException("request/user is required");
        }
        if (message.getRequest().getText() == null || message.getRequest().getText().isBlank()) {
            throw new IllegalArgumentException("text is required");
        }
    }

    public static String buildResponseXml(int code, String reason) {
        MessageDocument responseDocument = MessageDocument.Factory.newInstance();
        MessageType message = responseDocument.addNewMessage();
        message.addNewHeader().setTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

        ResponseType response = message.addNewResponse();

        StatusType status = response.addNewStatus();
        status.setCode(code);
        status.setReason(reason);

        return responseDocument.xmlText(PRETTY_XML);
    }
}