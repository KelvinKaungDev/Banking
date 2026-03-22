package com.chatbot.chatbot.service.implement;

import com.chatbot.chatbot.dto.Request.CustomerRequest;
import com.chatbot.chatbot.dto.Response.CustomerResponse;
import com.chatbot.chatbot.model.Customer;
import com.chatbot.chatbot.model.User;
import com.chatbot.chatbot.repository.CustomerRepository;
import com.chatbot.chatbot.repository.UserRepository;
import com.chatbot.chatbot.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Can't find the user:" + username));

        if (customerRepository.findByUser(user).isPresent()) {
            throw new RuntimeException("Customer already exists for this user");
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .user(user)
                .build();

        return mapToResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse getMyCustomer(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Customer customer = customerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + username));

        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return mapToResponse(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .build();
    }
}
