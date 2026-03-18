package com.chatbot.chatbot.service;

import com.chatbot.chatbot.dto.Request.CustomerRequest;
import com.chatbot.chatbot.dto.Response.CustomerResponse;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request, String username);

    CustomerResponse getMyCustomer(String username);

    CustomerResponse getCustomerById(Long id);
}
