package com.expensetracker.api.repository;

import com.expensetracker.api.entity.Category;
import com.expensetracker.api.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndBudgetGroupId(UUID id, UUID budgetGroupId);

    @Query("""
            select category
            from Category category
            where exists (
                select 1
                from BudgetMember member
                where member.budgetGroup = category.budgetGroup
                  and member.user.id = :userId
            )
              and (:budgetGroupId is null or category.budgetGroup.id = :budgetGroupId)
              and (:type is null or category.type = :type)
            order by category.name asc
            """)
    List<Category> findVisibleCategories(
            @Param("userId") UUID userId,
            @Param("budgetGroupId") UUID budgetGroupId,
            @Param("type") TransactionType type
    );
}
