package com.banking_app.bank.service.Impl.Impl;
import com.banking_app.bank.service.Impl.NotificationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class NotificationProducerImpl implements NotificationProducer {
    private static final String TOPIC = "transaction_notifications";
    private static final Logger logger = LoggerFactory.getLogger(NotificationProducerImpl.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    public NotificationProducerImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public boolean sendNotification(String message, String email, Double amount) {
        try {
            // Send Kafka notification
            kafkaTemplate.send(TOPIC, email,message + " : " + amount);
            logger.info("Notification sent to Kafka: " + message + " : " + amount);
            return true; //
        } catch (Exception e) {
            logger.error("Error sending notification: ", e);
            return false;
        }
    }
}
