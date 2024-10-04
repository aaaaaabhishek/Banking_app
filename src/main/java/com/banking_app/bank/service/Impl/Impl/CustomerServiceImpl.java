package com.banking_app.bank.service.Impl.Impl;

import com.banking_app.bank.Entity.Customer;
import com.banking_app.bank.Entity.CurrentAccount;
import com.banking_app.bank.Entity.SavingAccount;
import com.banking_app.bank.Exception.AccountNotFoundException;
import com.banking_app.bank.Payload.CustomerDto;
import com.banking_app.bank.Repositary.CustomerRepository;
import com.banking_app.bank.service.CustomerService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final ModelMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final NotificationProducerImpl notificationProducer;
//    private static final String ONBOARDING_TOPIC = "Onboarding is done"; // Constant for Kafka topic

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, ModelMapper mapper,
                               KafkaTemplate<String, String> kafkaTemplate, NotificationProducerImpl notificationProducer) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
        this.notificationProducer = notificationProducer;
    }

    @Transactional
    public CustomerDto onboardCustomer(CustomerDto customerDto) {
        System.out.println("CustomerDto before mapping: " + customerDto);
        Customer customer = mapper.map(customerDto, Customer.class);
        System.out.println("Customer after mapping: " + customer);

        double joiningBonus = 500.0;
        double currentBalance = 0.0;

        // Set customer ID
        customer.setCustomerId(UUID.randomUUID().toString());

        // Create CurrentAccount if it does not exist
        if (customer.getCurrentAccount().getAccountNumber() == null) {
            CurrentAccount currentAccount = new CurrentAccount();
            currentAccount.setBalance(currentBalance);
            currentAccount.setAccountNumber(UUID.randomUUID().toString()); // Ensure account number is generated
            customer.setCurrentAccount(currentAccount);
        }
        System.out.println("Customer after setting accounts: " + customer);

        // Create SavingAccount if it does not exist
        if (customer.getSavingAccount().getAccountNumber() == null) {
            SavingAccount savingAccount = new SavingAccount();
            savingAccount.setBalance(joiningBonus);  // Set joining bonus for Savings Account
            savingAccount.setAccountNumber(UUID.randomUUID().toString());  // Ensure account number is generated
            customer.setSavingAccount(savingAccount);
        }

        System.out.println("Customer after setting accounts: " + customer);

        String notificationMessage = "Onboarding successful for customer: " + customer.getName() + " with ID: " + customer.getCustomerId();
        String customerEmail = customer.getEmailId();
        System.out.println("Current Account Number: " + customer.getCurrentAccount().getAccountNumber());
        System.out.println("Saving Account Number: " + customer.getSavingAccount().getAccountNumber());

        // Validate email before sending notification
        if (customerEmail == null || customerEmail.isEmpty()) {
            throw new IllegalArgumentException("Customer email is required for notification");
        }

        // Send email notification
        boolean emailSent = notificationProducer.sendNotification(notificationMessage, customerEmail, joiningBonus);

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
    @Transactional
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
    @Transactional
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
