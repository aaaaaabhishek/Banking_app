package com.banking_app.bank.service.Impl;

import com.banking_app.bank.service.AccountService;

public interface SavingAccount extends AccountService {
    void deposits(Double amount,String accountNumber);
    Double withdraw(Double amount);
    String transferMoney(Long customerId, Double amount, String fromAccount, String toAccount);
}
