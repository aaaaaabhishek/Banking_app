package com.banking_app.bank.Exception;

public class NotificationSendFailedException extends RuntimeException{
    public NotificationSendFailedException(String message){
        super(message);
    }
}
