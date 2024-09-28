package com.banking_app.bank.Exception;

public class InvalidTransferException extends RuntimeException{
    public InvalidTransferException(String message){
        super(message);
    }
}
