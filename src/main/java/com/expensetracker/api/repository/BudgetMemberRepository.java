package com.expensetracker.api.repository;

import com.expensetracker.api.entity.BudgetGroup;
import com.expensetracker.api.entity.BudgetMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BudgetMemberRepository extends JpaRepository<BudgetMember, UUID> {

    boolean existsByBudgetGroupIdAndUserId(UUID budgetGroupId, UUID userId);

    void deleteAllByBudgetGroup(BudgetGroup budgetGroup);
}
