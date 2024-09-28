package com.banking_app.bank.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.processing.Generated;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "current_accounts")

public class CurrentAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    public Double balance;
public String accountType="cuurent";
    @Column(unique = true, nullable = false)
    public String accountNumber;

}
