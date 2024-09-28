package com.banking_app.bank.Exception;

public class AlreadyProceedTransactionException extends RuntimeException{
    public AlreadyProceedTransactionException(String message){
        super(message);
    }
}
