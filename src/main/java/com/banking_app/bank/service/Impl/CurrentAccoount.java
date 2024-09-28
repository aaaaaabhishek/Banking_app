package com.banking_app.bank.service.Impl;

import com.banking_app.bank.service.AccountService;

public  interface CurrentAccoount extends AccountService {
    void deposits(Double amount,String accountNumber);
    Double withdraw(Double amount);
    String transferMoney(Long customerId, Double amount, String fromAccount, String toAccount);

    String makePayment(Long customerId, Double amount, String toAccountNumber);

}
