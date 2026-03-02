package com.example.xmlserver.service;

import com.example.message.MessageType;
import com.example.xmlserver.model.XmlMessage;
import com.example.xmlserver.repository.XmlMessageRepository;
import com.example.xmlserver.util.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class XmlMessageService {
    private static final String SUCCESS_REASON = "success";
    private static final String INAPPROPRIATE_REASON = "used inappropriate language";
    private static final String INVALID_XML_REASON = "invalid xml request";

    private final XmlMessageRepository xmlMessageRepository;
    private final List<String> banWords;

    public XmlMessageService(
            XmlMessageRepository auditRepository,
            @Value("${xml.server.ban-words}'")
            String banWordsSource
    ) {
        this.xmlMessageRepository = auditRepository;
        this.banWords = loadBanWords(banWordsSource);
    }

    private List<String> loadBanWords(String banWordsSource) {
        if (banWordsSource == null || banWordsSource.isBlank()) {
            return List.of();
        }

        String trimmedSource = banWordsSource.trim();
        if (trimmedSource.startsWith("classpath:")) {
            return readWordsFromClasspath(trimmedSource);
        }

        Path filePath = toFilePath(banWordsSource);
        if (filePath != null) {
            try (Stream<String> lines = Files.lines(filePath)) {
                return normalizeWords(lines);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to read ban words file: " + filePath, ex);
            }
        }

        return normalizeWords(Stream.of(banWordsSource.split(",")));
    }

    private List<String> readWordsFromClasspath(String location) {
        String resourcePath = location.substring("classpath:".length()).replaceAll("'", "");
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }

        Resource resource = new ClassPathResource(resourcePath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Classpath resource not found: " + location);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return normalizeWords(reader.lines());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read classpath resource: " + location, ex);
        }
    }

    private static Path toFilePath(String source) {
        String trimmed = source.trim();
        if (trimmed.startsWith("file:")) {
            return Path.of(trimmed.substring("file:".length())).normalize();
        }

        Path path = Path.of(trimmed);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            return path.normalize();
        }

        return null;
    }

    private static List<String> normalizeWords(Stream<String> words) {
        return words
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .toList();
    }

    public XmlProcessingResult validateAndSave(String requestXml) {
        LocalTime messageTime = LocalTime.now();
        String user = null;
        String text = null;
        boolean isSuccesParse = false;
        int code;
        String reason;
        HttpStatus status = HttpStatus.OK;
        String responseXml;

        try {
            MessageType message = XmlUtils.parseRequest(requestXml);
            XmlUtils.validateMessageStructure(message);

            messageTime = toLocalTime(message.getHeader().getTime());
            user = message.getRequest().getUser();
            text = message.getRequest().getText();
            isSuccesParse = true;
            if (containsBanWord(text)) {
                code = 1;
                reason = INAPPROPRIATE_REASON;
            } else {
                code = 0;
                reason = SUCCESS_REASON;
            }
        } catch (XmlException | IllegalArgumentException ex) {
            code = 1;
            reason = INVALID_XML_REASON;
            status = HttpStatus.BAD_REQUEST;
        }

        responseXml = XmlUtils.buildResponseXml(code, reason);

        if(isSuccesParse) {
            xmlMessageRepository.save(
                    XmlMessage.createForMessage(messageTime, user, text, code));
        }
        return new XmlProcessingResult(status, responseXml);
    }

    private Boolean containsBanWord(String text) {
        if (text == null || text.isBlank() || banWords.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase(Locale.ROOT);
        return banWords.stream()
                .anyMatch(lowerText::contains);
    }

    private static LocalTime toLocalTime(Calendar calendar) {
        return calendar.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
