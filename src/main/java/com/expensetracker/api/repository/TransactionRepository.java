package com.expensetracker.api.repository;

import com.expensetracker.api.entity.Transaction;
import com.expensetracker.api.entity.TransactionType;
import com.expensetracker.api.repository.projection.CategoryReportRow;
import com.expensetracker.api.repository.projection.MemberReportRow;
import com.expensetracker.api.repository.projection.SummaryReportRow;
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

    @Query(value = """
            select
                min(t.transaction_date) as startDate,
                max(t.transaction_date) as endDate,
                coalesce(sum(case when t.type = 'INCOME' then t.amount else 0 end), 0) as totalIncome,
                coalesce(sum(case when t.type = 'EXPENSE' then t.amount else 0 end), 0) as totalExpense
            from transactions t
            where exists (
                select 1
                from budget_members bm
                where bm.budget_group_id = t.budget_group_id
                  and bm.user_id = :userId
            )
              and (:budgetGroupId is null or t.budget_group_id = :budgetGroupId)
              and (:startDate is null or t.transaction_date >= :startDate)
              and (:endDate is null or t.transaction_date <= :endDate)
            """, nativeQuery = true)
    SummaryReportRow getSummaryReport(
            @Param("userId") UUID userId,
            @Param("budgetGroupId") UUID budgetGroupId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
            select
                c.id as categoryId,
                c.name as categoryName,
                t.type as type,
                coalesce(sum(t.amount), 0) as totalAmount,
                count(t.id) as transactionCount
            from transactions t
            join categories c on c.id = t.category_id
            where exists (
                select 1
                from budget_members bm
                where bm.budget_group_id = t.budget_group_id
                  and bm.user_id = :userId
            )
              and (:budgetGroupId is null or t.budget_group_id = :budgetGroupId)
              and (:startDate is null or t.transaction_date >= :startDate)
              and (:endDate is null or t.transaction_date <= :endDate)
              and (:type is null or t.type = cast(:type as varchar))
            group by c.id, c.name, t.type
            order by c.name asc, t.type asc
            """, nativeQuery = true)
    java.util.List<CategoryReportRow> getCategoryReport(
            @Param("userId") UUID userId,
            @Param("budgetGroupId") UUID budgetGroupId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") String type
    );

    @Query(value = """
            select
                bm.id as memberId,
                u.id as userId,
                u.first_name as firstName,
                u.last_name as lastName,
                coalesce(sum(case when t.type = 'INCOME' then t.amount else 0 end), 0) as totalIncome,
                coalesce(sum(case when t.type = 'EXPENSE' then t.amount else 0 end), 0) as totalExpense,
                count(t.id) as transactionCount
            from transactions t
            join budget_members bm on bm.id = t.member_id
            join users u on u.id = bm.user_id
            where exists (
                select 1
                from budget_members current_member
                where current_member.budget_group_id = t.budget_group_id
                  and current_member.user_id = :userId
            )
              and (:budgetGroupId is null or t.budget_group_id = :budgetGroupId)
              and (:startDate is null or t.transaction_date >= :startDate)
              and (:endDate is null or t.transaction_date <= :endDate)
            group by bm.id, u.id, u.first_name, u.last_name
            order by u.first_name asc, u.last_name asc
            """, nativeQuery = true)
    java.util.List<MemberReportRow> getMemberReport(
            @Param("userId") UUID userId,
            @Param("budgetGroupId") UUID budgetGroupId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
