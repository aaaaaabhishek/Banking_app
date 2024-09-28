package com.banking_app.bank.service.Impl.Impl;

import com.banking_app.bank.Entity.Customer;
import com.banking_app.bank.Exception.*;
import com.banking_app.bank.Repositary.CustomerRepositary;
import com.banking_app.bank.service.I_NotificationProducer;
import com.banking_app.bank.service.Impl.CurrentAccoount;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;
@Service
public class CureentAccountService implements CurrentAccoount {
    private final CustomerRepositary customerRepositary;
    private final ModelMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final I_NotificationProducer notificationProducer;
    private final Set<String> processedTransactions = new HashSet<>();

    @Autowired
    public CureentAccountService(CustomerRepositary customerRepositary, ModelMapper mapper, I_NotificationProducer notificationProducer,KafkaTemplate<String, String> kafkaTemplate) {
        this.customerRepositary = customerRepositary;
        this.mapper = mapper;
        this.notificationProducer = notificationProducer;
        this.kafkaTemplate = kafkaTemplate;

    }

    @Override
    @Transactional
    public void deposits(Double amount, String accountNumber) {
        Customer customer = customerRepositary.findByCurrentAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account with account number " + accountNumber + " not found"));
        String transactionId = generateIdempotencyKey(amount, accountNumber);
        if (isTransactionAlreadyProcessed(transactionId)) {
            return;  // Transaction already processed, skip
        }
        // Correct string comparison for account type
        if ("current".equals(customer.getCurrentAccount().getAccountType())) {
            Double balance = customer.getCurrentAccount().getBalance();
            customer.getCurrentAccount().setBalance(balance + amount);
            customerRepositary.save(customer);
        } else {
            throw new RuntimeException("Account type mismatch. Expected: 'current', but found: " + customer.getCurrentAccount().getAccountType());
        }
    }

    @Override
    public Double withdraw(Double amount) {
        throw new UnsupportedOperationException("Make payment operation is not implemented yet");
    }
    @Override
    @Transactional
    public String transferMoney(Long customerId, Double amount, String fromAccount, String toAccount) {
        String transactionId = generateIdempotencyKey(amount, fromAccount + toAccount);  // Generate unique key for this transfer
        if (isTransactionAlreadyProcessed(transactionId)) {
            return "Transaction already processed.";  // Transaction already processed, return
        }

        if (fromAccount == null || toAccount == null) {
            throw new AccountNotFoundException("Please provide both the " + fromAccount + " and " + toAccount + " account numbers.");
        }

        Customer fromAccountCustomer = customerRepositary.findByCurrentAccountNumber(fromAccount)
                .orElseThrow(() -> new RuntimeException("Account with account number " + fromAccount + " not found"));

        Customer toAccountCustomer = customerRepositary.findByCurrentAccountNumber(toAccount)
                .orElseThrow(() -> new RuntimeException("Account with account number " + toAccount + " not found"));

        if (fromAccountCustomer.equals(toAccountCustomer)) {
            if (fromAccountCustomer.getSavingAccount() == null) {
                throw new AccountNotFoundException("No saving account found for this customer.");
            }

            Double total_balance = fromAccountCustomer.getCurrentAccount().getBalance();
            Double fee = total_balance * 0.0005;
            Double deduction = amount + fee;
            if (total_balance < deduction) {
                throw new InsufficientBalanceException("Insufficient balance");
            }

            fromAccountCustomer.getCurrentAccount().setBalance(total_balance - deduction);
            fromAccountCustomer.getSavingAccount().setBalance(fromAccountCustomer.getSavingAccount().getBalance() + amount);
            customerRepositary.save(fromAccountCustomer);

            // Send Notification
            String notificationMessage = "Debited " + deduction + " (including fee) from " + fromAccount + " and credited " + amount + " to savings account.";
            notificationProducer.sendNotification(notificationMessage, fromAccountCustomer.getEmailId(), amount);

            // Mark this transaction as processed
            processedTransactions.add(transactionId);

            return notificationMessage;
        }

        String accountType = toAccountCustomer.getCurrentAccount().getAccountNumber().equals(toAccount) ? "current" : "saving";
        Double total_balance = fromAccountCustomer.getCurrentAccount().getBalance();
        Double fee = total_balance * 0.0005;
        Double deduction = amount + fee;
        if (total_balance < deduction) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        fromAccountCustomer.getCurrentAccount().setBalance(total_balance - deduction);

        if ("current".equals(accountType)) {
            toAccountCustomer.getCurrentAccount().setBalance(toAccountCustomer.getCurrentAccount().getBalance() + amount);
        } else if ("saving".equals(accountType)) {
            toAccountCustomer.getSavingAccount().setBalance(toAccountCustomer.getSavingAccount().getBalance() + amount);
        } else {
            throw new InvalidAccountTypeException("Invalid account type for transfer.");
        }

        customerRepositary.save(fromAccountCustomer);
        customerRepositary.save(toAccountCustomer);

        kafkaTemplate.send("transaction_notifications", "Transfer successful from " + fromAccount + " to " + toAccount);

        // Send Notification
        String notificationMessage = "Transferred " + amount + " from " + fromAccount + " to " + toAccount + ". Fee: " + fee + ". Total deduction: " + deduction;
        notificationProducer.sendNotification(notificationMessage, fromAccountCustomer.getEmailId(), amount);

        // Mark this transaction as processed
        processedTransactions.add(transactionId);

        return accountType + " transfer successful.";
    }

    @Transactional
    @Override
    public String makePayment(Long customerId, Double amount, String toAccountNumber) {
        String idempotencyKey = generateIdempotencyKey(amount, toAccountNumber);
        if (isTransactionAlreadyProcessed(idempotencyKey)) {
            throw new AlreadyProceedTransactionException("This transaction has already been processed");
        }

        Customer fromCustomer = customerRepositary.findById(customerId)
                .orElseThrow(() -> new AccountNotFoundException(customerId + " is not present in our database"));
        Customer toCustomer = customerRepositary.findByCurrentAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(toAccountNumber + " This account number customer is not present in our database"));

        Double fromCurrentBalance = fromCustomer.getCurrentAccount().getBalance();
        if (fromCurrentBalance < amount || amount <= 0) {
            throw new InsufficientBalanceException("Insufficient funds or invalid amount");
        }

        // Handling transfer to the correct account type
        if (toCustomer.getSavingAccount() != null && toCustomer.getSavingAccount().getAccountNumber().equals(toAccountNumber)) {
            // To Savings Account
            Double toSavingsBalance = toCustomer.getSavingAccount().getBalance();
            toCustomer.getSavingAccount().setBalance(toSavingsBalance + amount);
        } else if (toCustomer.getCurrentAccount() != null && toCustomer.getCurrentAccount().getAccountNumber().equals(toAccountNumber)) {
            // To Current Account
            Double toCurrentBalance = toCustomer.getCurrentAccount().getBalance();
            toCustomer.getCurrentAccount().setBalance(toCurrentBalance + amount);
        } else {
            throw new InvalidAccountTypeException("Invalid account number for receiving payment");
        }
        // Fee Calculation
        Double fee = fromCurrentBalance * 0.0005;
        Double totalDeduction = amount + fee;
        if (fromCurrentBalance < totalDeduction) {
            throw new InsufficientBalanceException("Insufficient balance to cover both the transfer and the fee");
        }

        fromCustomer.getCurrentAccount().setBalance(fromCurrentBalance - totalDeduction);
        customerRepositary.save(fromCustomer);
        customerRepositary.save(toCustomer);

        String notificationMessage = "Transfer of " + amount + " from Customer ID: " + customerId + " to Account Number: " + toAccountNumber + " completed.";
        String customerEmail = fromCustomer.getEmailId();

        boolean notificationSent = notificationProducer.sendNotification(notificationMessage, customerEmail, amount);
        if (!notificationSent) {
            throw new NotificationSendFailedException("Failed to send notification for the payment");
        }

        // Mark transaction as processed after successful save and notification
        processedTransactions.add(idempotencyKey);

        return notificationMessage;
    }
    private String generateIdempotencyKey(Double amount, String accountNumber) {
        return accountNumber + "-" + amount + "-" + System.currentTimeMillis();
    }
    private boolean isTransactionAlreadyProcessed(String idempotencyKey) {
        return processedTransactions.contains(idempotencyKey);
    }
}
