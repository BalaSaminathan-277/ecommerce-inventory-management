package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Product - used in API requests and responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private Integer quantityInStock;
    private Integer reorderLevel;
    private Boolean isActive;
    private Boolean isLowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
