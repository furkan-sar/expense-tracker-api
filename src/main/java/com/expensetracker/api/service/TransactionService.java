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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetMemberRepository budgetMemberRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

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
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate", "createdAt"));
        Page<Transaction> transactions = transactionRepository.findVisibleTransactions(
                currentUser.getId(),
                budgetGroupId,
                categoryId,
                toEntityType(type),
                startDate,
                endDate,
                pageRequest
        );

        return new TransactionPageResponse(
                transactions.getContent().stream().map(transactionMapper::toResponse).toList(),
                transactions.getNumber(),
                transactions.getSize(),
                transactions.getTotalElements(),
                transactions.getTotalPages()
        );
    }

    @Transactional
    public TransactionResponse createTransaction(User currentUser, TransactionCreateRequest request) {
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

        return transactionMapper.toResponse(transactionRepository.save(transaction));
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
                .orElseThrow(() -> new NotFoundException("Transaction was not found."));
    }

    private BudgetGroup requireBudgetGroup(UUID budgetGroupId) {
        return budgetGroupRepository.findById(budgetGroupId)
                .orElseThrow(() -> new NotFoundException("Budget group was not found."));
    }

    private BudgetMember requireMemberAccess(BudgetGroup budgetGroup, User currentUser) {
        return budgetMemberRepository.findByBudgetGroupIdAndUserId(budgetGroup.getId(), currentUser.getId())
                .orElseThrow(() -> new AuthorizationException("Authenticated user is not allowed to access this resource."));
    }

    private Category requireCategoryInBudgetGroup(UUID categoryId, UUID budgetGroupId) {
        return categoryRepository.findByIdAndBudgetGroupId(categoryId, budgetGroupId)
                .orElseThrow(() -> new NotFoundException("Category was not found."));
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
