package com.expensetracker.api.repository;

import com.expensetracker.api.entity.BudgetGroup;
import com.expensetracker.api.entity.BudgetMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetMemberRepository extends JpaRepository<BudgetMember, UUID> {

    boolean existsByBudgetGroupIdAndUserId(UUID budgetGroupId, UUID userId);

    Optional<BudgetMember> findByBudgetGroupIdAndUserId(UUID budgetGroupId, UUID userId);

    Optional<BudgetMember> findByIdAndBudgetGroupId(UUID id, UUID budgetGroupId);

    @Query("""
            select member
            from BudgetMember member
            join fetch member.user
            where member.budgetGroup.id = :budgetGroupId
            order by member.joinedAt asc
            """)
    List<BudgetMember> findAllByBudgetGroupIdWithUser(@Param("budgetGroupId") UUID budgetGroupId);

    void deleteAllByBudgetGroup(BudgetGroup budgetGroup);
}
