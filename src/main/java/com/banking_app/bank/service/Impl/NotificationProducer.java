package com.banking_app.bank.service.Impl;

import com.banking_app.bank.service.I_NotificationProducer;

public interface NotificationProducer extends I_NotificationProducer {
    public boolean sendNotification(String message, String email, Double amount);
    }
