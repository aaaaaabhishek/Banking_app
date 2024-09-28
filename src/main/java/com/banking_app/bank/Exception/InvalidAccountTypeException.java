package com.banking_app.bank.Exception;

public class InvalidAccountTypeException extends RuntimeException{
    public InvalidAccountTypeException(String message){
        super(message);
    }
}
