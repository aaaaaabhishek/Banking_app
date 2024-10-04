package com.banking_app.bank.Payload;

import com.banking_app.bank.Entity.CurrentAccount;
import com.banking_app.bank.Entity.SavingAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    public String customerId;  // Add this line
    public String name;
    public long age;
    public String emailId;
    public CurrentAccount currentAccount;
    public SavingAccount savingAccount;
}

