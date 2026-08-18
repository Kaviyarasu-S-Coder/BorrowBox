package com.borrowbox.service;

public interface EmailService {

    void sendSimpleEmail(String toEmail, String subject, String body);

    void sendHtmlEmail(String toEmail, String subject, String htmlContent);
}
