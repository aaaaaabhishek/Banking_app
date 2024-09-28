package com.banking_app.bank.controller;

import com.banking_app.bank.service.Impl.CurrentAccoount;
import com.banking_app.bank.service.Impl.SavingAccount;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CurrentAccoount currentAccountService;
    private final SavingAccount savingAccountService;

    public AccountController(CurrentAccoount currentAccountService, SavingAccount savingAccountService) {
        this.currentAccountService = currentAccountService;
        this.savingAccountService = savingAccountService;
    }

    @PostMapping("/deposits/current")
    public ResponseEntity<String> depositToCurrentAccount(@RequestParam Long customerId,
                                                          @RequestParam Double amount,
                                                          @RequestParam String accountNumber) {
        try {
            currentAccountService.deposits(amount, accountNumber);
            return new ResponseEntity<>("Deposit successful to Current Account.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/deposits/savings")
    public ResponseEntity<String> depositToSavingAccount(@RequestParam Long customerId,
                                                         @RequestParam Double amount,
                                                         @RequestParam String accountNumber) {
        try {
            savingAccountService.deposits(amount, accountNumber);
            return new ResponseEntity<>("Deposit successful to Savings Account.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestParam Long customerId,
                                                @RequestParam Double amount,
                                                @RequestParam String fromAccount, // "current" or "savings"
                                                @RequestParam String toAccount) { // "current" or "savings"
        try {
            String result = currentAccountService.transferMoney(customerId, amount, fromAccount, toAccount);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Make payment from Current Account
    @PostMapping("/payment")
    public ResponseEntity<String> makePayment(@RequestParam Long customerId,
                                              @RequestParam Double amount,
                                              @RequestParam String toAccountNumber) {
        try {
            String result = currentAccountService.makePayment(customerId, amount, toAccountNumber);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
