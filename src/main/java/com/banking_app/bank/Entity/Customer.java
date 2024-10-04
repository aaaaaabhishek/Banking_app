package com.banking_app.bank.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="customer")
public class Customer {
    @Id
    public String customerId;
    public String name;
    public long age;
    public String emailId;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "current_account_id") // Specify the foreign key column
    private CurrentAccount currentAccount;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "saving_account_id") // Specify the foreign key column for savingAccount
    private SavingAccount savingAccount;
}
