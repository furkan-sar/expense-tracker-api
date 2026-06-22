package com.expensetracker.api.service;

import com.expensetracker.api.dto.TransactionCreateRequest;
import com.expensetracker.api.dto.TransactionPageResponse;
import com.expensetracker.api.dto.TransactionResponse;
import com.expensetracker.api.dto.TransactionUpdateRequest;
import com.expensetracker.api.entity.BudgetGroup;
import com.expensetracker.api.entity.BudgetMember;
import com.expensetracker.api.entity.Category;
import com.expensetracker.api.entity.Transaction;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.exception.AuthorizationException;
import com.expensetracker.api.exception.NotFoundException;
import com.expensetracker.api.mapper.TransactionMapper;
import com.expensetracker.api.repository.BudgetGroupRepository;
import com.expensetracker.api.repository.BudgetMemberRepository;
import com.expensetracker.api.repository.CategoryRepository;
import com.expensetracker.api.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetMemberRepository budgetMemberRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public TransactionPageResponse listTransactions(
            User currentUser,
            UUID budgetGroupId,
            UUID categoryId,
            com.expensetracker.api.dto.TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        log.info("service=TransactionService action=listTransactions start userId={} budgetGroupId={} categoryId={} page={} size={}", currentUser.getId(), budgetGroupId, categoryId, page, size);
        try {
            if (budgetGroupId != null && !budgetMemberRepository.existsByBudgetGroupIdAndUserId(budgetGroupId, currentUser.getId())) {
                throw new AuthorizationException("Authenticated user is not allowed to access this resource.");
            }
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate", "createdAt"));
            StringBuilder jpql = new StringBuilder("""
                    select transaction
                    from Transaction transaction
                    where exists (
                        select 1
                        from BudgetMember currentMember
                        where currentMember.budgetGroup = transaction.budgetGroup
                          and currentMember.user.id = :userId
                    )
                    """);
            if (budgetGroupId != null) {
                jpql.append(" and transaction.budgetGroup.id = :budgetGroupId");
            }
            if (categoryId != null) {
                jpql.append(" and transaction.category.id = :categoryId");
            }
            if (type != null) {
                jpql.append(" and transaction.type = :type");
            }
            if (startDate != null) {
                jpql.append(" and transaction.transactionDate >= :startDate");
            }
            if (endDate != null) {
                jpql.append(" and transaction.transactionDate <= :endDate");
            }
            jpql.append(" order by transaction.transactionDate desc, transaction.createdAt desc");

            var query = entityManager.createQuery(jpql.toString(), Transaction.class);
            query.setParameter("userId", currentUser.getId());
            if (budgetGroupId != null) {
                query.setParameter("budgetGroupId", budgetGroupId);
            }
            if (categoryId != null) {
                query.setParameter("categoryId", categoryId);
            }
            if (type != null) {
                query.setParameter("type", toEntityType(type));
            }
            if (startDate != null) {
                query.setParameter("startDate", startDate);
            }
            if (endDate != null) {
                query.setParameter("endDate", endDate);
            }
            List<Transaction> allResults = query.getResultList();
            int total = allResults.size();
            List<Transaction> content = allResults.stream()
                    .skip(pageRequest.getOffset())
                    .limit(pageRequest.getPageSize())
                    .toList();
            Page<Transaction> transactions = new org.springframework.data.domain.PageImpl<>(content, pageRequest, total);

            TransactionPageResponse response = new TransactionPageResponse(
                    transactions.getContent().stream().map(transactionMapper::toResponse).toList(),
                    transactions.getNumber(),
                    transactions.getSize(),
                    transactions.getTotalElements(),
                    transactions.getTotalPages()
            );
            log.info("service=TransactionService action=listTransactions success count={}", response.content().size());
            return response;
        } catch (RuntimeException ex) {
            log.error("service=TransactionService action=listTransactions failure userId={}", currentUser.getId(), ex);
            throw ex;
        }
    }

    @Transactional
    public TransactionResponse createTransaction(User currentUser, TransactionCreateRequest request) {
        log.info("service=TransactionService action=createTransaction start userId={} budgetGroupId={} categoryId={}", currentUser.getId(), request.budgetGroupId(), request.categoryId());
        try {
            BudgetGroup budgetGroup = requireBudgetGroup(request.budgetGroupId());
            BudgetMember currentMember = requireMemberAccess(budgetGroup, currentUser);
            Category category = requireCategoryInBudgetGroup(request.categoryId(), budgetGroup.getId());

            Transaction transaction = new Transaction();
            transaction.setBudgetGroup(budgetGroup);
            transaction.setCategory(category);
            transaction.setMember(currentMember);
            transaction.setType(toEntityType(request.type()));
            transaction.setAmount(request.amount());
            transaction.setTransactionDate(request.transactionDate());
            transaction.setNote(normalizeNullable(request.note()));

            TransactionResponse response = transactionMapper.toResponse(transactionRepository.save(transaction));
            log.info("service=TransactionService action=createTransaction success transactionId={}", response.id());
            return response;
        } catch (RuntimeException ex) {
            log.error("service=TransactionService action=createTransaction failure budgetGroupId={} categoryId={}", request.budgetGroupId(), request.categoryId(), ex);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(User currentUser, UUID id) {
        Transaction transaction = requireTransaction(id);
        requireMemberAccess(transaction.getBudgetGroup(), currentUser);
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(User currentUser, UUID id, TransactionUpdateRequest request) {
        Transaction transaction = requireTransaction(id);
        requireMemberAccess(transaction.getBudgetGroup(), currentUser);
        Category category = requireCategoryInBudgetGroup(request.categoryId(), transaction.getBudgetGroup().getId());

        transaction.setCategory(category);
        transaction.setType(toEntityType(request.type()));
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setNote(normalizeNullable(request.note()));

        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public void deleteTransaction(User currentUser, UUID id) {
        Transaction transaction = requireTransaction(id);
        requireMemberAccess(transaction.getBudgetGroup(), currentUser);
        transactionRepository.delete(transaction);
    }

    private Transaction requireTransaction(UUID id) {
        return transactionRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Transaction was not found.", id == null ? null : id.toString()));
    }

    private BudgetGroup requireBudgetGroup(UUID budgetGroupId) {
        return budgetGroupRepository.findById(budgetGroupId)
                .orElseThrow(() -> new NotFoundException("Budget group was not found.", budgetGroupId == null ? null : budgetGroupId.toString()));
    }

    private BudgetMember requireMemberAccess(BudgetGroup budgetGroup, User currentUser) {
        return budgetMemberRepository.findByBudgetGroupIdAndUserId(budgetGroup.getId(), currentUser.getId())
                .orElseThrow(() -> new AuthorizationException("Authenticated user is not allowed to access this resource."));
    }

    private Category requireCategoryInBudgetGroup(UUID categoryId, UUID budgetGroupId) {
        return categoryRepository.findByIdAndBudgetGroupId(categoryId, budgetGroupId)
                .orElseThrow(() -> new NotFoundException("Category was not found.", categoryId == null ? null : categoryId.toString()));
    }

    private com.expensetracker.api.entity.TransactionType toEntityType(com.expensetracker.api.dto.TransactionType type) {
        return type == null ? null : com.expensetracker.api.entity.TransactionType.valueOf(type.name());
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
