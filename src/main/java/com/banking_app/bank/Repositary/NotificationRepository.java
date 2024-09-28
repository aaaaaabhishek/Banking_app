package com.banking_app.bank.Repositary;

import com.banking_app.bank.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}