package com.ecom.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}")
    private String mailUsername;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        log.info("Dispatching email notification to: [{}] Subject: [{}]", to, subject);

        if (mailSender != null && mailUsername != null && !mailUsername.isBlank()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(mailUsername);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
                log.info("Email successfully delivered via SMTP to {}", to);
            } catch (Exception e) {
                log.warn("Failed to dispatch email via SMTP (logged instead): {}", e.getMessage());
            }
        } else {
            log.info("[Dev Mode / No SMTP Credentials] Simulated Email to {}: Subject='{}' | Content='{}'", to, subject, text);
        }
    }
}
