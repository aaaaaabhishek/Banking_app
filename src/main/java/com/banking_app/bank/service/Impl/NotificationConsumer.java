package com.banking_app.bank.service.Impl;

import com.banking_app.bank.service.I_NotificationConsumer;

public interface NotificationConsumer extends I_NotificationConsumer {
    public void listen(String message);

    public void handleNotification(String message, Double amount);

    public void saveToDatabase(String message, Double amount);
}
