package com.banking_app.bank.service.Impl;

import com.banking_app.bank.Payload.CustomerDto;
import com.banking_app.bank.service.CustomerService;

import java.util.List;

public interface I_CustomerService extends CustomerService {
    public CustomerDto onboardCustomer(CustomerDto customerDto);

    public CustomerDto getOnboardCustomer(Long id);

    public List<CustomerDto> getAllOnboardCustomers();

    public CustomerDto updateCustomer(Long id, CustomerDto customerDto);

    public CustomerDto updateCustomerByField(Long id, CustomerDto customerDto);
}
