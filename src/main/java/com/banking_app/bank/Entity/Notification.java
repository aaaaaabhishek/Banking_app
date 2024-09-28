package com.banking_app.bank.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "message", nullable = false)
    private String message;
    @Column(name = "amount", nullable = false)
    private Double amount;
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    public Notification(String message, Double amount) {
        this.message = message;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
}