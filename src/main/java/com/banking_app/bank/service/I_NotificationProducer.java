package com.banking_app.bank.service;

import com.banking_app.bank.service.Impl.NotificationProducer;

public   interface I_NotificationProducer  {
    public boolean sendNotification(String message, String email, Double amount);
}

