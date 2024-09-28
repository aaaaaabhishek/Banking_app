package com.banking_app.bank.service.Impl.Impl;

import com.banking_app.bank.Entity.Notification;
import com.banking_app.bank.Repositary.NotificationRepository;
import com.banking_app.bank.service.Impl.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumerImpl implements NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumerImpl.class);

    @Autowired
    public NotificationConsumerImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "transaction_notifications", groupId = "notification-group")
    public void listen(String message) {
        logger.info("Notification Received: " + message);
        String[] parts = message.split(":");
        if (parts.length < 2) {
            logger.error("Invalid message format received: " + message);
            return; // Skip processing if message format is incorrect
        }
        String notificationMessage = parts[0];
        Double amount;
        try {
            amount = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            logger.error("Invalid amount format in message: " + message, e);
            return; // Skip processing if amount format is incorrect
        }
        handleNotification(notificationMessage, amount);
    }

    public void handleNotification(String message, Double amount) {
        saveToDatabase(message, amount);
    }

    public void saveToDatabase(String message, Double amount) {
        // Create a new Notification object
        Notification notification = new Notification(message, amount);
        // Save the notification to the database using the repository
        notificationRepository.save(notification);
        logger.info("Notification saved to DB: " + message + " Amount: " + amount);
    }
}
