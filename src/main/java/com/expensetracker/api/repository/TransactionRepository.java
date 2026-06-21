package com.expensetracker.api.repository;

import com.expensetracker.api.entity.Transaction;
import com.expensetracker.api.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @EntityGraph(attributePaths = {
            "budgetGroup",
            "category",
            "category.budgetGroup",
            "member",
            "member.user",
            "member.budgetGroup"
    })
    @Query("""
            select transaction
            from Transaction transaction
            where exists (
                select 1
                from BudgetMember currentMember
                where currentMember.budgetGroup = transaction.budgetGroup
                  and currentMember.user.id = :userId
            )
              and (:budgetGroupId is null or transaction.budgetGroup.id = :budgetGroupId)
              and (:categoryId is null or transaction.category.id = :categoryId)
              and (:type is null or transaction.type = :type)
              and (:startDate is null or transaction.transactionDate >= :startDate)
              and (:endDate is null or transaction.transactionDate <= :endDate)
            """)
    Page<Transaction> findVisibleTransactions(
            @Param("userId") UUID userId,
            @Param("budgetGroupId") UUID budgetGroupId,
            @Param("categoryId") UUID categoryId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "budgetGroup",
            "category",
            "category.budgetGroup",
            "member",
            "member.user",
            "member.budgetGroup"
    })
    Optional<Transaction> findWithDetailsById(UUID id);
}
