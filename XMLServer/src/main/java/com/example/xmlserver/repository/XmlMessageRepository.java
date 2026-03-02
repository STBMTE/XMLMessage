package com.example.xmlserver.repository;

import com.example.xmlserver.model.XmlMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface XmlMessageRepository extends JpaRepository<XmlMessage, Long> {
    Optional<XmlMessage> findTopByOrderByIdDesc();
}
