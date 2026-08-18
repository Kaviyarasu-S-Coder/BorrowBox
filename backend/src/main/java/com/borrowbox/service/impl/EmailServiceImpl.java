package com.borrowbox.service.impl;

import com.borrowbox.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    @Override
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        if (mailSender == null) {
            log.info("[MOCK EMAIL DISPATCH] To: {}, Subject: '{}', Body: '{}'", toEmail, subject, body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("notifications@borrowbox.com");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Sent email to: {}", toEmail);
        } catch (Exception ex) {
            log.warn("Could not send email to {}: {}", toEmail, ex.getMessage());
        }
    }

    @Async
    @Override
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        log.info("[MOCK HTML EMAIL DISPATCH] To: {}, Subject: '{}'", toEmail, subject);
    }
}
