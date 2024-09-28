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
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long customerId;
    public String name;
    public long age;
    @Email
    public String emailId;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CurrentAccount currentAccount;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SavingAccount savingAccount;
}
