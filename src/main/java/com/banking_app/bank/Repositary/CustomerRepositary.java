package com.banking_app.bank.Repositary;

import com.banking_app.bank.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepositary extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCurrentAccountNumber(String accountNumber);
}
