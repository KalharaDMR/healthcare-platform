package com.healthcare.notification_service.service.impl;

import com.healthcare.notification_service.service.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name:Healthcare Platform}")
    private String fromName;

    @PostConstruct
    public void validateConfig() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY is missing");
        }
    }

    @Override
    public void sendEmail(String to, String subject, String message) {
        Email from = new Email(fromEmail, fromName);
        Email recipient = new Email(to);
        Content content = new Content("text/plain", message);
        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sendGrid = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");

        try {
            request.setBody(mail.build()); // can throw IOException
            Response response = sendGrid.api(request); // can throw IOException

            int status = response.getStatusCode();
            if (status < 200 || status >= 300) {
                throw new RuntimeException(
                        "SendGrid failed. HTTP " + status + ", body: " + response.getBody()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to call SendGrid API", e);
        }
    }
}