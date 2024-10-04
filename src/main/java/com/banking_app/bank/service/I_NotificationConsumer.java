package com.banking_app.bank.service;

public interface I_NotificationConsumer {
    public void listen(String message);

    public void handleNotification(String email,String message, Double amount);

    public void saveToDatabase(String message, Double amount);
}