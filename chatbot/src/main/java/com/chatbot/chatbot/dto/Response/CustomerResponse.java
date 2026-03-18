package com.chatbot.chatbot.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
}