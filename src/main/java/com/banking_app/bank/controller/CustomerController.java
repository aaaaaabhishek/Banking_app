package com.banking_app.bank.controller;

import com.banking_app.bank.Payload.CustomerDto;
import com.banking_app.bank.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.naming.Binding;
import java.util.List;

@RestController
@RequestMapping("/customers") // Base URL for the controller
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<?> onboardCustomer(@Valid @RequestBody CustomerDto customerDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Return validation errors and bad request status
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        CustomerDto createdCustomer = customerService.onboardCustomer(customerDto);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getOnboardCustomer(@PathVariable Long id) {
        CustomerDto customerDto = customerService.getOnboardCustomer(id);
        return new ResponseEntity<>(customerDto, HttpStatus.OK);
    }

    @GetMapping("/allCustomer")
    public ResponseEntity<List<CustomerDto>> getAllOnboardCustomers() {
        List<CustomerDto> customers = customerService.getAllOnboardCustomers();
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @RequestBody CustomerDto customerDto) {
        CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<CustomerDto> updateCustomerByField(@PathVariable Long id, @RequestBody CustomerDto customerDto) {
        CustomerDto updatedCustomer = customerService.updateCustomerByField(id, customerDto);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }
}
