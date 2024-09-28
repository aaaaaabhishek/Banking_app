package com.banking_app.bank.Exception;

public class SameAccountException extends RuntimeException{
    public SameAccountException(String message){
    super(message);}
}
