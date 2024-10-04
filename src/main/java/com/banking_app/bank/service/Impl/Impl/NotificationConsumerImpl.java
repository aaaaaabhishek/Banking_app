package com.banking_app.bank.service.Impl.Impl;

import com.banking_app.bank.Entity.Notification;
import com.banking_app.bank.Repositary.NotificationRepository;
import com.banking_app.bank.service.Impl.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumerImpl implements NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumerImpl.class);
    private final JavaMailSender mailSender;

    @Autowired
    public NotificationConsumerImpl(NotificationRepository notificationRepository, JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = "transaction_notifications", groupId = "notification-group")
    public void listen(String message) {
        logger.info("Notification Received: " + message);
        String[] parts = message.split(":");

        if (parts.length < 3) {
            logger.error("Invalid message format received: " + message);
            return;
        }

        String email = parts[0].trim(); // Extract email

        String notificationMessage = parts[1];
        Double amount;

        try {
            amount = Double.parseDouble(parts[2].trim());
        } catch (NumberFormatException e) {
            logger.error("Invalid amount format in message: " + message, e);
            return;
        }

        handleNotification(email, notificationMessage, amount);
    }

    @Override
    public void handleNotification(String email, String message, Double amount) {
        saveToDatabase(message, amount);
        sendEmailNotification(email, message, amount);
    }

    public void saveToDatabase(String message, Double amount) {
        Notification notification = new Notification(message, amount);
        notificationRepository.save(notification);
        logger.info("Notification saved to DB: " + message + " Amount: " + amount);
    }

    public void sendEmailNotification(String toEmail, String message, Double amount) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject("Transaction Notification");
            String text = (amount != null) ? (message + " Amount deducted: " + amount) : message;
            mailMessage.setText(text);
            mailSender.send(mailMessage);
            logger.info("Email sent to: " + toEmail);
        } catch (Exception e) {
            logger.error("Error sending email notification to " + toEmail, e);
        }
    }
}