package com.expensetracker.api.repository;

import com.expensetracker.api.entity.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
    Optional<Transaction> findWithDetailsById(UUID id);
}
