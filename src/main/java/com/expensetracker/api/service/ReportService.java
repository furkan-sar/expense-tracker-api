package com.expensetracker.api.service;

import com.expensetracker.api.dto.CategoryReportItem;
import com.expensetracker.api.dto.MemberReportItem;
import com.expensetracker.api.dto.SummaryReportResponse;
import com.expensetracker.api.dto.TransactionType;
import com.expensetracker.api.entity.BudgetGroup;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.exception.AuthorizationException;
import com.expensetracker.api.exception.NotFoundException;
import com.expensetracker.api.repository.BudgetGroupRepository;
import com.expensetracker.api.repository.BudgetMemberRepository;
import com.expensetracker.api.repository.TransactionRepository;
import com.expensetracker.api.repository.projection.CategoryReportRow;
import com.expensetracker.api.repository.projection.MemberReportRow;
import com.expensetracker.api.repository.projection.SummaryReportRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final TransactionRepository transactionRepository;
    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetMemberRepository budgetMemberRepository;

    @Transactional(readOnly = true)
    public SummaryReportResponse getSummaryReport(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate) {
        BudgetGroup budgetGroup = requireReportBudgetGroup(currentUser, budgetGroupId);
        SummaryReportRow row = transactionRepository.getSummaryReport(currentUser.getId(), budgetGroup.getId(), startDate, endDate);

        BigDecimal totalIncome = amountOrZero(row.getTotalIncome());
        BigDecimal totalExpense = amountOrZero(row.getTotalExpense());
        LocalDate effectiveStartDate = firstNonNull(startDate, row.getStartDate(), LocalDate.now());
        LocalDate effectiveEndDate = firstNonNull(endDate, row.getEndDate(), effectiveStartDate);

        return new SummaryReportResponse(
                budgetGroup.getId(),
                effectiveStartDate,
                effectiveEndDate,
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                budgetGroup.getCurrency()
        );
    }

    @Transactional(readOnly = true)
    public List<CategoryReportItem> getCategoryReport(
            User currentUser,
            UUID budgetGroupId,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type
    ) {
        UUID effectiveBudgetGroupId = requireOptionalBudgetGroupAccess(currentUser, budgetGroupId);
        return transactionRepository.getCategoryReport(
                        currentUser.getId(),
                        effectiveBudgetGroupId,
                        startDate,
                        endDate,
                        type == null ? null : type.name()
                )
                .stream()
                .map(this::toCategoryReportItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberReportItem> getMemberReport(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate) {
        UUID effectiveBudgetGroupId = requireOptionalBudgetGroupAccess(currentUser, budgetGroupId);
        return transactionRepository.getMemberReport(currentUser.getId(), effectiveBudgetGroupId, startDate, endDate)
                .stream()
                .map(this::toMemberReportItem)
                .toList();
    }

    private BudgetGroup requireReportBudgetGroup(User currentUser, UUID budgetGroupId) {
        if (budgetGroupId == null) {
            return budgetGroupRepository.findAllByMemberUserId(currentUser.getId())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Budget group was not found."));
        }

        BudgetGroup budgetGroup = budgetGroupRepository.findById(budgetGroupId)
                .orElseThrow(() -> new NotFoundException("Budget group was not found."));
        requireMemberAccess(currentUser, budgetGroup.getId());
        return budgetGroup;
    }

    private UUID requireOptionalBudgetGroupAccess(User currentUser, UUID budgetGroupId) {
        if (budgetGroupId == null) {
            return null;
        }

        if (!budgetGroupRepository.existsById(budgetGroupId)) {
            throw new NotFoundException("Budget group was not found.");
        }

        requireMemberAccess(currentUser, budgetGroupId);
        return budgetGroupId;
    }

    private void requireMemberAccess(User currentUser, UUID budgetGroupId) {
        if (!budgetMemberRepository.existsByBudgetGroupIdAndUserId(budgetGroupId, currentUser.getId())) {
            throw new AuthorizationException("Authenticated user is not allowed to access this resource.");
        }
    }

    private CategoryReportItem toCategoryReportItem(CategoryReportRow row) {
        return new CategoryReportItem(
                row.getCategoryId(),
                row.getCategoryName(),
                TransactionType.valueOf(row.getType().name()),
                amountOrZero(row.getTotalAmount()),
                row.getTransactionCount()
        );
    }

    private MemberReportItem toMemberReportItem(MemberReportRow row) {
        return new MemberReportItem(
                row.getMemberId(),
                row.getUserId(),
                row.getFirstName(),
                row.getLastName(),
                amountOrZero(row.getTotalIncome()),
                amountOrZero(row.getTotalExpense()),
                row.getTransactionCount()
        );
    }

    private BigDecimal amountOrZero(BigDecimal amount) {
        return amount == null ? ZERO : amount;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
