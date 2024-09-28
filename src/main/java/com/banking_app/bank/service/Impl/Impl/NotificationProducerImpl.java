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
    private final JavaMailSender mailSender;
    @Autowired
    public NotificationProducerImpl(KafkaTemplate<String, String> kafkaTemplate, JavaMailSender mailSender) {
        this.kafkaTemplate = kafkaTemplate;
        this.mailSender = mailSender;
    }
    public boolean sendNotification(String message, String email, Double amount) {
        try {
            // Send Kafka notification
            kafkaTemplate.send(TOPIC, message + " : " + amount);
            sendEmailNotification(email, message, amount);
            return true; // If both Kafka and email were sent successfully
        } catch (Exception e) {
            logger.error("Error sending notification: ", e);
            return false;
        }
    }
    public boolean sendNotification(String message, String email) {
        return sendNotification(message, email, null);
    }
    public void sendEmailNotification(String toEmail, String message, Double amount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("Transaction Notification");
        String text = (amount != null) ? (message + " Amount deducted: " + amount) : message;
        mailMessage.setText(text);
        mailSender.send(mailMessage);
    }
}
