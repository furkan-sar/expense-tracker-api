package com.expensetracker.api.repository;

import com.expensetracker.api.entity.BudgetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetGroupRepository extends JpaRepository<BudgetGroup, UUID> {

    @Query("""
            select distinct budgetGroup
            from BudgetGroup budgetGroup
            join budgetGroup.members member
            where member.user.id = :userId
            order by budgetGroup.createdAt desc
            """)
    List<BudgetGroup> findAllByMemberUserId(@Param("userId") UUID userId);

    @Query("""
            select distinct budgetGroup
            from BudgetGroup budgetGroup
            left join fetch budgetGroup.members members
            left join fetch members.user
            where budgetGroup.id = :id
            """)
    Optional<BudgetGroup> findByIdWithMembers(@Param("id") UUID id);
}
