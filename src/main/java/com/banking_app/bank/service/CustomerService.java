package com.banking_app.bank.service;

import com.banking_app.bank.Payload.CustomerDto;

import java.util.List;

public interface CustomerService {
    public CustomerDto onboardCustomer(CustomerDto customerDto);

    public CustomerDto getOnboardCustomer(Long id);

    public List<CustomerDto> getAllOnboardCustomers();

    public CustomerDto updateCustomer(Long id, CustomerDto customerDto);

    public CustomerDto updateCustomerByField(Long id, CustomerDto customerDto);
}