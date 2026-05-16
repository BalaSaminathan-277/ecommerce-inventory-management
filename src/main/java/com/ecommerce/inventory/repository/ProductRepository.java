package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ProductRepository - Data access layer for Product entity.
 * Contains advanced queries for searching and filtering products.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Find all active products with pagination
     */
    Page<Product> findByIsActiveTrue(Pageable pageable);

    /**
     * Search products by name
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isActive = true")
    List<Product> searchByName(@Param("name") String name);

    /**
     * Search products by description
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%')) AND p.isActive = true")
    List<Product> searchByDescription(@Param("description") String description);

    /**
     * Find products by price range
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.quantityInStock <= p.reorderLevel AND p.isActive = true")
    List<Product> findLowStockProducts();

    /**
     * Find out of stock products
     */
    @Query("SELECT p FROM Product p WHERE p.quantityInStock = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();

    /**
     * Find products by category
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    List<Product> findByCategory(@Param("categoryId") Long categoryId);

    /**
     * Advanced search by name or description with pagination
     */
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.isActive = true")
    Page<Product> advancedSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products with low stock count
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantityInStock <= p.reorderLevel AND p.isActive = true")
    long countLowStockProducts();

    /**
     * Find products in specific price range with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.price >= :minPrice AND p.price <= :maxPrice AND p.isActive = true")
    Page<Product> findByPriceRangePageable(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    /**
     * Find products by category with pagination
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    Page<Product> findByCategoryPageable(@Param("categoryId") Long categoryId, Pageable pageable);
}
