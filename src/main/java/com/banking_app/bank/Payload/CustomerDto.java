package com.banking_app.bank.Payload;

import com.banking_app.bank.Entity.CurrentAccount;
import com.banking_app.bank.Entity.SavingAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    public Long customerId;
    public String name;
    public long age;
    @OneToOne(cascade= CascadeType.ALL,fetch = FetchType.LAZY)
    public CurrentAccount currentAccount;
    @OneToOne(cascade= CascadeType.ALL,fetch = FetchType.LAZY)
    public SavingAccount savingAccount;
}

