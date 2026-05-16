package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CategoryRepository - Data access layer for Category entity.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);

    /**
     * Check if category exists by name
     */
    boolean existsByName(String name);
}
