package com.chatbot.chatbot.controller;

import com.chatbot.chatbot.dto.Request.CustomerRequest;
import com.chatbot.chatbot.dto.Response.CustomerResponse;
import com.chatbot.chatbot.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest customerRequest, Principal principal) {
        return ResponseEntity.ok(customerService.createCustomer(customerRequest, principal.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyCustomer(Principal principal) {

        return ResponseEntity.ok(
                customerService.getMyCustomer(principal.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {

        return ResponseEntity.ok(
                customerService.getCustomerById(id)
        );
    }
}
