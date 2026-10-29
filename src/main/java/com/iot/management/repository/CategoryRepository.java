package com.iot.management.repository;

import com.iot.management.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByDeletedFalseOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
    java.util.Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
