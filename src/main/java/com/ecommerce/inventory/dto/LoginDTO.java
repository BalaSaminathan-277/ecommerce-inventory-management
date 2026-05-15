package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User login - used in API requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    private String username;
    private String password;
}
