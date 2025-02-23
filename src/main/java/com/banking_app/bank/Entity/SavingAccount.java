package com.banking_app.bank.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "saving_accounts")
public class SavingAccount {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long id;
    public Double balance;
    public String accountType="saving";
    @Column(unique = true, nullable = false)
    public String accountNumber;
}
