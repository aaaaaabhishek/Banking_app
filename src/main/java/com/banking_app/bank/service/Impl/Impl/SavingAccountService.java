package com.banking_app.bank.service.Impl.Impl;

import com.banking_app.bank.Entity.Customer;
import com.banking_app.bank.Exception.*;
import com.banking_app.bank.Repositary.CustomerRepositary;
import com.banking_app.bank.service.AccountService;
import com.banking_app.bank.service.Impl.SavingAccount;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashSet;
import java.util.Set;

public class SavingAccountService implements SavingAccount {
    private final CustomerRepositary customerRepositary;
    private final ModelMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Set<String> processedTransactions = new HashSet<>(); // Track processed transactions

    public SavingAccountService(CustomerRepositary customerRepositary, ModelMapper mapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.customerRepositary = customerRepositary;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
    }
    @Override
    @Transactional
    public void deposits(Double amount, String accountNumber) {
        Customer customer = customerRepositary.findByCurrentAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with account number " + accountNumber + " not found"));
        String transactionId = generateIdempotencyKey(amount, accountNumber);
        if (isTransactionAlreadyProcessed(transactionId)) {
            return;  // Transaction already processed, skip
        }
        // Corrected String comparison for account type
        if ("savings".equals(customer.getSavingAccount().getAccountType())) {
            Double saving_balance = customer.getSavingAccount().getBalance();
            // Add interest and update balance
            Double new_balance = saving_balance + (saving_balance * 0.005);  // Assuming 0.5% interest
            customer.getSavingAccount().setBalance(new_balance + amount);
        }
        customerRepositary.save(customer);

        // Mark transaction as processed
        processedTransactions.add(transactionId);

        // Send notification via Kafka or another messaging system
        kafkaTemplate.send("transaction_notifications", "Deposit successful to Savings Account: " + accountNumber);
    }

    @Override
    public Double withdraw(Double amount) {
        throw new UnsupportedOperationException("Withdraw operation is not implemented yet");
    }

    @Override
    @Transactional
    public String transferMoney(Long customerId, Double amount, String fromAccountnumber, String toAccountnumber) {
        if (fromAccountnumber == null || toAccountnumber == null) {
            throw new InvalidTransferException("Account numbers must not be null");
        }
        Customer fromAccountNumber = customerRepositary.findByCurrentAccountNumber(fromAccountnumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with account number " + fromAccountnumber + " not found"));
        Customer toAccountNumber = customerRepositary.findByCurrentAccountNumber(toAccountnumber)
                .orElseThrow(() -> new AccountNotFoundException("Account with account number " + toAccountnumber + " not found"));
        if(!fromAccountNumber.equals(toAccountNumber)) throw new InvalidTransferException("Threre is two different customermer");
        if (fromAccountNumber.getSavingAccount().getAccountNumber().equals(toAccountNumber.getCurrentAccount().getAccountNumber())) {
            throw new SameAccountException("Money can't be transferred because it's the same account number");
        }

        String accountType = fromAccountNumber.getSavingAccount().getAccountType();
        if ("saving".equalsIgnoreCase(accountType)) {
            Double fromBalance = fromAccountNumber.getSavingAccount().getBalance();
            Double toBalance = toAccountNumber.getCurrentAccount().getBalance();
            if (fromBalance < amount) throw new InsufficientBalanceException("Insufficient balance");
            fromAccountNumber.getSavingAccount().setBalance(fromBalance - amount);
            toAccountNumber.getCurrentAccount().setBalance(toBalance + amount);
        }
        // Save the updated balances
        customerRepositary.save(fromAccountNumber);
        customerRepositary.save(toAccountNumber);

        // Send Kafka notification
        kafkaTemplate.send("transaction_notifications", "Transfer successful from " + fromAccountnumber + " to " + toAccountnumber);

        return accountType + " transfer successful";
    }
    @Override
    public String makePayment(Long customerId, Double amount, String toAccountNumber) {
        throw new UnsupportedOperationException("Make payment operation is not implemented yet");
    }
    private String generateIdempotencyKey(Double amount, String accountNumber) {
        // Generate a unique key for the transaction based on timestamp, account, and amount
        return accountNumber + "-" + amount + "-" + System.nanoTime();
    }
    private boolean isTransactionAlreadyProcessed(String idempotencyKey) {
        // Check if the transaction with this idempotency key has already been processed
        return processedTransactions.contains(idempotencyKey);
    }
}
