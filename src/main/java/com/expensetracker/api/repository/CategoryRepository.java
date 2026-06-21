package com.expensetracker.api.repository;

import com.expensetracker.api.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndBudgetGroupId(UUID id, UUID budgetGroupId);
}
