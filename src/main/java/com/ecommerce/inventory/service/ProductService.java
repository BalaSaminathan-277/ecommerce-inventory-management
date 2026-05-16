package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.ProductDTO;
import com.ecommerce.inventory.entity.Category;
import com.ecommerce.inventory.entity.Product;
import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.exception.ValidationException;
import com.ecommerce.inventory.repository.CategoryRepository;
import com.ecommerce.inventory.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductService - Business logic layer for Product operations.
 * Handles CRUD operations, inventory management, and product searches.
 */
@Service
@Transactional
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Create a new product
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product with SKU: {}", productDTO.getSku());

        // Validate input
        if (productDTO.getSku() == null || productDTO.getSku().trim().isEmpty()) {
            throw new ValidationException("sku", "Product SKU is required");
        }

        // Check if SKU already exists
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new ValidationException("sku", "Product with SKU already exists: " + productDTO.getSku());
        }

        // Fetch category
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));

        // Create new product
        Product product = new Product();
        product.setSku(productDTO.getSku());
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setCategory(category);
        product.setPrice(productDTO.getPrice());
        product.setQuantityInStock(productDTO.getQuantityInStock());
        product.setReorderLevel(productDTO.getReorderLevel());
        product.setIsActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return convertToDTO(savedProduct);
    }

    /**
     * Get product by ID
     */
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return convertToDTO(product);
    }

    /**
     * Get all products with pagination
     */
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.info("Fetching all active products with pagination");
        return productRepository.findByIsActiveTrue(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Update product
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Update fields
        if (productDTO.getName() != null) {
            product.setName(productDTO.getName());
        }
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getPrice() != null) {
            product.setPrice(productDTO.getPrice());
        }
        if (productDTO.getReorderLevel() != null) {
            product.setReorderLevel(productDTO.getReorderLevel());
        }
        if (productDTO.getIsActive() != null) {
            product.setIsActive(productDTO.getIsActive());
        }

        product.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(product);

        log.info("Product updated successfully with ID: {}", id);
        return convertToDTO(updatedProduct);
    }

    /**
     * Delete product (soft delete - mark as inactive)
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        log.info("Product deleted successfully with ID: {}", id);
    }

    /**
     * Search products by name
     */
    public List<ProductDTO> searchByName(String name) {
        log.info("Searching products by name: {}", name);
        return productRepository.searchByName(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search products by description
     */
    public List<ProductDTO> searchByDescription(String description) {
        log.info("Searching products by description");
        return productRepository.searchByDescription(description).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find products by price range
     */
    public List<ProductDTO> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Finding products in price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock products
     */
    public List<ProductDTO> getLowStockProducts() {
        log.info("Fetching low stock products");
        return productRepository.findLowStockProducts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get out of stock products
     */
    public List<ProductDTO> getOutOfStockProducts() {
        log.info("Fetching out of stock products");
        return productRepository.findOutOfStockProducts().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Reduce product quantity (used during order processing)
     */
    public void reduceQuantity(Long productId, Integer quantity) {
        log.info("Reducing quantity for product ID: {} by: {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (quantity > product.getQuantityInStock()) {
            throw new ValidationException("quantity", 
                    "Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getQuantityInStock() + ", Requested: " + quantity);
        }

        product.reduceQuantity(quantity);
        productRepository.save(product);
        log.info("Quantity reduced successfully for product ID: {}", productId);
    }

    /**
     * Increase product quantity (used during stock adjustment)
     */
    public void increaseQuantity(Long productId, Integer quantity) {
        log.info("Increasing quantity for product ID: {} by: {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        product.increaseQuantity(quantity);
        productRepository.save(product);
        log.info("Quantity increased successfully for product ID: {}", productId);
    }

    /**
     * Advanced search
     */
    public Page<ProductDTO> advancedSearch(String searchTerm, Pageable pageable) {
        log.info("Performing advanced search with term: {}", searchTerm);
        return productRepository.advancedSearch(searchTerm, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Convert Product entity to ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setPrice(product.getPrice());
        dto.setQuantityInStock(product.getQuantityInStock());
        dto.setReorderLevel(product.getReorderLevel());
        dto.setIsActive(product.getIsActive());
        dto.setIsLowStock(product.isLowStock());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}
