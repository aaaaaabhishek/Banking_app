package com.banking_app.bank.service.Impl.Impl;

import com.banking_app.bank.Entity.Customer;
import com.banking_app.bank.Entity.CurrentAccount;
import com.banking_app.bank.Entity.SavingAccount;
import com.banking_app.bank.Exception.AccountNotFoundException;
import com.banking_app.bank.Payload.CustomerDto;
import com.banking_app.bank.Repositary.CustomerRepositary;
import com.banking_app.bank.service.CustomerService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepositary customerRepository;
    private final ModelMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final NotificationProducerImpl notificationProducer;
    private static final String ONBOARDING_TOPIC = "Onboarding is done"; // Constant for Kafka topic

    @Autowired
    public CustomerServiceImpl(CustomerRepositary customerRepository, ModelMapper mapper,
                               KafkaTemplate<String, String> kafkaTemplate, NotificationProducerImpl notificationProducer) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
        this.notificationProducer = notificationProducer;
    }

    @Transactional
    public CustomerDto onboardCustomer(CustomerDto customerDto) {
        Customer customer = mapper.map(customerDto, Customer.class);
        double joiningBonus = 500.0;
        double currentBalance = 0.0;

        // Ensure account balances are set
        if (customer.getCurrentAccount() == null) {
            CurrentAccount currentAccount = new CurrentAccount();
            currentAccount.setBalance(currentBalance);
            currentAccount.setAccountNumber(UUID.randomUUID().toString()); // Generate random account number
            customer.setCurrentAccount(currentAccount);
        }
        if (customer.getSavingAccount() == null) {
            SavingAccount savingAccount = new SavingAccount();
            savingAccount.setBalance(joiningBonus);  // Set joining bonus for Savings Account
            savingAccount.setAccountNumber(UUID.randomUUID().toString());  // Generate random account number
            customer.setSavingAccount(savingAccount);
        }

        String notificationMessage = "Onboarding successful for customer: " + customer.getName() + " with ID: " + customer.getCustomerId();
        String customerEmail = customer.getEmailId();

        // Validate email before sending notification
        if (customerEmail == null || customerEmail.isEmpty()) {
            throw new IllegalArgumentException("Customer email is required for notification");
        }

        // Send message to Kafka
        CompletableFuture<SendResult<String, String>> notificationSent = kafkaTemplate.send(ONBOARDING_TOPIC, customerEmail)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        // Handle exception for Kafka send failure
                        System.err.println("Failed to send Kafka notification: " + exception.getMessage());
                    }
                });

        // Send email notification
        boolean emailSent = notificationProducer.sendNotification(notificationMessage, customerEmail);

        // Save the customer to the repository
        Customer savedCustomer = customerRepository.save(customer);

        // Optionally, you could check if the email was sent successfully and handle accordingly
        if (!emailSent) {
            System.err.println("Email notification failed for customer: " + customer.getName());
        }

        return mapper.map(savedCustomer, CustomerDto.class);
    }

    public CustomerDto getOnboardCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id + " not Found"));
        return mapper.map(customer, CustomerDto.class);
    }

    public List<CustomerDto> getAllOnboardCustomers() {
        List<Customer> allCustomers = customerRepository.findAll();
        return allCustomers.stream()
                .map(customer -> mapper.map(customer, CustomerDto.class))
                .collect(Collectors.toList());
    }

    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id + " does not exist in our database"));

        // Update customer fields
        customer.setName(customerDto.getName());
        customer.setAge(customerDto.getAge());
        customer.setCurrentAccount(mapper.map(customerDto.getCurrentAccount(), CurrentAccount.class));
        customer.setSavingAccount(mapper.map(customerDto.getSavingAccount(), SavingAccount.class));

        Customer updatedCustomer = customerRepository.save(customer);
        return mapper.map(updatedCustomer, CustomerDto.class);
    }
    public CustomerDto updateCustomerByField(Long id, CustomerDto customerDto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id + " does not exist in our database"));
        // Update fields conditionally
        if (customerDto.getName() != null) {
            customer.setName(customerDto.getName());
        }
        if (customerDto.getAge() > 0) {
            customer.setAge(customerDto.getAge());
        }
        if (customerDto.getCurrentAccount() != null) {
            customer.setCurrentAccount(mapper.map(customerDto.getCurrentAccount(), CurrentAccount.class));
        }
        if (customerDto.getSavingAccount() != null) {
            customer.setSavingAccount(mapper.map(customerDto.getSavingAccount(), SavingAccount.class));
        }
        Customer updatedCustomer = customerRepository.save(customer);
        return mapper.map(updatedCustomer, CustomerDto.class);
    }
}
