package com.expensetracker.api.service;

import com.expensetracker.api.dto.*;
import com.expensetracker.api.entity.BudgetGroup;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.exception.AuthorizationException;
import com.expensetracker.api.exception.NotFoundException;
import com.expensetracker.api.repository.BudgetGroupRepository;
import com.expensetracker.api.repository.BudgetMemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetMemberRepository budgetMemberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public SummaryReportResponse getSummaryReport(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID memberId, TransactionType transactionType) {
        ReportQueryFilter filter = normalizeFilter(currentUser, budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
        SummaryRowData row = fetchSummaryRow(filter);
        String currency = resolveCurrency(filter);
        String budgetGroupName = resolveBudgetGroupName(filter);
        BigDecimal income = amountOrZero(row.totalIncome());
        BigDecimal expense = amountOrZero(row.totalExpense());
        BigDecimal balance = income.subtract(expense);
        BigDecimal savingsRate = income.signum() == 0 ? ZERO : balance.max(ZERO).multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP);
        return new SummaryReportResponse(filter.budgetGroupId(), budgetGroupName, filter.startDate(), filter.endDate(), income, expense, balance, currency, savingsRate);
    }

    @Transactional(readOnly = true)
    public List<CategoryReportItem> getCategoryReport(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID memberId, TransactionType transactionType) {
        ReportQueryFilter filter = normalizeFilter(currentUser, budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
        List<CategoryRowData> rows = fetchCategoryRows(filter);
        BigDecimal total = rows.stream().map(CategoryRowData::totalAmount).map(this::amountOrZero).reduce(ZERO, BigDecimal::add);
        String currency = resolveCurrency(filter);
        return rows.stream().map(row -> new CategoryReportItem(row.categoryId(), row.categoryName(), row.type() == null ? null : TransactionType.valueOf(row.type()), amountOrZero(row.totalAmount()), percentageOf(amountOrZero(row.totalAmount()), total), currency)).toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseTrendReportItem> getExpenseTrendReport(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID memberId, TransactionType transactionType) {
        ReportQueryFilter filter = normalizeFilter(currentUser, budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
        String currency = resolveCurrency(filter);
        return fetchExpenseTrendRows(filter).stream().map(row -> new ExpenseTrendReportItem(row.date(), amountOrZero(row.income()), amountOrZero(row.expense()), amountOrZero(row.income()).subtract(amountOrZero(row.expense())), currency)).toList();
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryReportItem> getMonthlySummaryReport(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID memberId, TransactionType transactionType) {
        ReportQueryFilter filter = normalizeFilter(currentUser, budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
        String currency = resolveCurrency(filter);
        return fetchMonthlyRows(filter).stream().map(row -> new MonthlySummaryReportItem(row.month(), amountOrZero(row.income()), amountOrZero(row.expense()), amountOrZero(row.income()).subtract(amountOrZero(row.expense())), currency)).toList();
    }

    @Transactional(readOnly = true)
    public List<MemberSpendingReportItem> getMemberReport(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID memberId, TransactionType transactionType) {
        ReportQueryFilter filter = normalizeFilter(currentUser, budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
        List<MemberRowData> rows = fetchMemberRows(filter);
        BigDecimal total = rows.stream().map(MemberRowData::totalExpense).map(this::amountOrZero).reduce(ZERO, BigDecimal::add);
        String currency = resolveCurrency(filter);
        return rows.stream().map(row -> new MemberSpendingReportItem(row.memberId(), row.firstName() + " " + row.lastName(), amountOrZero(row.totalExpense()), percentageOf(amountOrZero(row.totalExpense()), total), currency)).toList();
    }

    @Transactional(readOnly = true)
    public DashboardReportResponse getDashboardReport(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID memberId, TransactionType transactionType) {
        ReportQueryFilter filter = normalizeFilter(currentUser, budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
        SummaryRowData summaryRow = fetchSummaryRow(filter);
        List<CategoryReportItem> categoryBreakdown = getCategoryReport(currentUser, filter.budgetGroupId(), filter.startDate(), filter.endDate(), filter.categoryId(), filter.memberId(), filter.transactionType());
        List<ExpenseTrendReportItem> trend = getExpenseTrendReport(currentUser, filter.budgetGroupId(), filter.startDate(), filter.endDate(), filter.categoryId(), filter.memberId(), filter.transactionType());
        List<MonthlySummaryReportItem> monthlySummary = getMonthlySummaryReport(currentUser, filter.budgetGroupId(), filter.startDate(), filter.endDate(), filter.categoryId(), filter.memberId(), filter.transactionType());
        List<MemberSpendingReportItem> memberSpending = getMemberReport(currentUser, filter.budgetGroupId(), filter.startDate(), filter.endDate(), filter.categoryId(), filter.memberId(), filter.transactionType());
        BigDecimal income = amountOrZero(summaryRow.totalIncome());
        BigDecimal expense = amountOrZero(summaryRow.totalExpense());
        BigDecimal balance = income.subtract(expense);
        BigDecimal savingsRate = income.signum() == 0 ? ZERO : balance.max(ZERO).multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP);
        DashboardSummaryWidgets summary = new DashboardSummaryWidgets(resolveCurrency(filter), income, expense, balance, savingsRate, summaryRow.transactionCount(), categoryBreakdown.stream().findFirst().orElse(null), memberSpending.stream().findFirst().orElse(null));
        return new DashboardReportResponse(filter.budgetGroupId(), resolveBudgetGroupName(filter), filter.startDate(), filter.endDate(), resolveCurrency(filter), summary, categoryBreakdown, trend, monthlySummary, memberSpending);
    }

    private ReportQueryFilter normalizeFilter(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID memberId, TransactionType transactionType) {
        LocalDate normalizedEnd = endDate == null ? LocalDate.now() : endDate;
        LocalDate normalizedStart = startDate == null ? normalizedEnd.with(TemporalAdjusters.firstDayOfMonth()) : startDate;
        if (budgetGroupId != null) {
            if (!budgetGroupRepository.existsById(budgetGroupId)) {
                throw new NotFoundException("Budget group was not found.", budgetGroupId.toString());
            }
            requireMemberAccess(currentUser, budgetGroupId);
        }
        return new ReportQueryFilter(currentUser, budgetGroupId, normalizedStart, normalizedEnd, categoryId, memberId, transactionType);
    }

    private SummaryRowData fetchSummaryRow(ReportQueryFilter filter) {
        StringBuilder sql = new StringBuilder("""
                select min(t.transaction_date) as start_date, max(t.transaction_date) as end_date,
                       coalesce(sum(case when t.type = 'INCOME' then t.amount else 0 end), 0) as total_income,
                       coalesce(sum(case when t.type = 'EXPENSE' then t.amount else 0 end), 0) as total_expense,
                       count(t.id) as transaction_count
                from transactions t
                where exists (select 1 from budget_members bm where bm.budget_group_id = t.budget_group_id and bm.user_id = :userId)
                """);
        appendFilters(sql, filter);
        var q = entityManager.createNativeQuery(sql.toString());
        bindFilters(q, filter);
        Object[] row = (Object[]) q.getSingleResult();
        return new SummaryRowData(toLocalDate(row[0]), toLocalDate(row[1]), (BigDecimal) row[2], (BigDecimal) row[3], ((Number) row[4]).longValue());
    }

    private List<CategoryRowData> fetchCategoryRows(ReportQueryFilter filter) {
        StringBuilder sql = new StringBuilder("""
                select c.id, c.name, t.type, coalesce(sum(t.amount), 0)
                from transactions t
                join categories c on c.id = t.category_id
                where exists (select 1 from budget_members bm where bm.budget_group_id = t.budget_group_id and bm.user_id = :userId)
                """);
        appendFilters(sql, filter);
        sql.append(" group by c.id, c.name, t.type order by c.name asc, t.type asc");
        var q = entityManager.createNativeQuery(sql.toString());
        bindFilters(q, filter);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new CategoryRowData((UUID) r[0], (String) r[1], r[2] == null ? null : r[2].toString(), (BigDecimal) r[3])).toList();
    }

    private List<TrendRowData> fetchExpenseTrendRows(ReportQueryFilter filter) {
        StringBuilder sql = new StringBuilder("""
                select t.transaction_date, coalesce(sum(case when t.type = 'INCOME' then t.amount else 0 end), 0), coalesce(sum(case when t.type = 'EXPENSE' then t.amount else 0 end), 0)
                from transactions t
                where exists (select 1 from budget_members bm where bm.budget_group_id = t.budget_group_id and bm.user_id = :userId)
                """);
        appendFilters(sql, filter);
        sql.append(" group by t.transaction_date order by t.transaction_date asc");
        var q = entityManager.createNativeQuery(sql.toString());
        bindFilters(q, filter);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new TrendRowData(toLocalDate(r[0]), (BigDecimal) r[1], (BigDecimal) r[2])).toList();
    }

    private List<MonthRowData> fetchMonthlyRows(ReportQueryFilter filter) {
        StringBuilder sql = new StringBuilder("""
                select to_char(date_trunc('month', t.transaction_date), 'YYYY-MM'), coalesce(sum(case when t.type = 'INCOME' then t.amount else 0 end), 0), coalesce(sum(case when t.type = 'EXPENSE' then t.amount else 0 end), 0)
                from transactions t
                where exists (select 1 from budget_members bm where bm.budget_group_id = t.budget_group_id and bm.user_id = :userId)
                """);
        appendFilters(sql, filter);
        sql.append(" group by date_trunc('month', t.transaction_date) order by date_trunc('month', t.transaction_date) asc");
        var q = entityManager.createNativeQuery(sql.toString());
        bindFilters(q, filter);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new MonthRowData((String) r[0], (BigDecimal) r[1], (BigDecimal) r[2])).toList();
    }

    private List<MemberRowData> fetchMemberRows(ReportQueryFilter filter) {
        StringBuilder sql = new StringBuilder("""
                select bm.id, u.first_name, u.last_name, coalesce(sum(case when t.type = 'EXPENSE' then t.amount else 0 end), 0)
                from transactions t
                join budget_members bm on bm.id = t.member_id
                join users u on u.id = bm.user_id
                where exists (select 1 from budget_members current_member where current_member.budget_group_id = t.budget_group_id and current_member.user_id = :userId)
                """);
        appendFilters(sql, filter);
        sql.append(" group by bm.id, u.first_name, u.last_name order by u.first_name asc, u.last_name asc");
        var q = entityManager.createNativeQuery(sql.toString());
        bindFilters(q, filter);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows.stream().map(r -> new MemberRowData((UUID) r[0], (String) r[1], (String) r[2], (BigDecimal) r[3])).toList();
    }

    private void appendFilters(StringBuilder sql, ReportQueryFilter filter) {
        if (filter.budgetGroupId() != null) sql.append(" and t.budget_group_id = :budgetGroupId");
        if (filter.categoryId() != null) sql.append(" and t.category_id = :categoryId");
        if (filter.memberId() != null) sql.append(" and t.member_id = :memberId");
        if (filter.transactionType() != null) sql.append(" and t.type = :transactionType");
        if (filter.startDate() != null) sql.append(" and t.transaction_date >= :startDate");
        if (filter.endDate() != null) sql.append(" and t.transaction_date <= :endDate");
    }

    private void bindFilters(Object query, ReportQueryFilter filter) {
        jakarta.persistence.Query q = (jakarta.persistence.Query) query;
        q.setParameter("userId", filter.currentUser().getId());
        if (filter.budgetGroupId() != null) q.setParameter("budgetGroupId", filter.budgetGroupId());
        if (filter.categoryId() != null) q.setParameter("categoryId", filter.categoryId());
        if (filter.memberId() != null) q.setParameter("memberId", filter.memberId());
        if (filter.transactionType() != null) q.setParameter("transactionType", filter.transactionType().name());
        if (filter.startDate() != null) q.setParameter("startDate", filter.startDate());
        if (filter.endDate() != null) q.setParameter("endDate", filter.endDate());
    }

    private String resolveCurrency(ReportQueryFilter filter) {
        if (filter.budgetGroupId() != null) {
            return resolveCurrencyForBudgetGroup(filter.budgetGroupId());
        }
        return resolveCurrencyForAccessibleGroups(filter.currentUser());
    }

    private String resolveBudgetGroupName(ReportQueryFilter filter) {
        if (filter.budgetGroupId() != null) {
            return budgetGroupRepository.findById(filter.budgetGroupId()).orElseThrow(() -> new NotFoundException("Budget group was not found.", filter.budgetGroupId().toString())).getName();
        }
        return "All Budget Groups";
    }

    private String resolveCurrencyForBudgetGroup(UUID budgetGroupId) {
        return budgetGroupRepository.findById(budgetGroupId).orElseThrow(() -> new NotFoundException("Budget group was not found.", budgetGroupId.toString())).getCurrency();
    }

    private String resolveCurrencyForAccessibleGroups(User currentUser) {
        List<BudgetGroup> groups = budgetGroupRepository.findAllByMemberUserId(currentUser.getId());
        if (groups.isEmpty()) {
            throw new NotFoundException("Budget group was not found.");
        }
        return groups.get(0).getCurrency();
    }

    private void requireMemberAccess(User currentUser, UUID budgetGroupId) {
        if (!budgetMemberRepository.existsByBudgetGroupIdAndUserId(budgetGroupId, currentUser.getId())) {
            throw new AuthorizationException("Authenticated user is not allowed to access this resource.");
        }
    }

    private BigDecimal amountOrZero(BigDecimal amount) {
        return amount == null ? ZERO : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal total) {
        if (total == null || total.signum() == 0) return ZERO;
        return amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate localDate) return localDate;
        if (value instanceof java.sql.Date sqlDate) return sqlDate.toLocalDate();
        throw new ClassCastException("Unsupported date type: " + value.getClass().getName());
    }

    private record ReportQueryFilter(User currentUser, UUID budgetGroupId, LocalDate startDate, LocalDate endDate, UUID categoryId, UUID memberId, TransactionType transactionType) {}
    private record SummaryRowData(LocalDate startDate, LocalDate endDate, BigDecimal totalIncome, BigDecimal totalExpense, long transactionCount) {}
    private record CategoryRowData(UUID categoryId, String categoryName, String type, BigDecimal totalAmount) {}
    private record TrendRowData(LocalDate date, BigDecimal income, BigDecimal expense) {}
    private record MonthRowData(String month, BigDecimal income, BigDecimal expense) {}
    private record MemberRowData(UUID memberId, String firstName, String lastName, BigDecimal totalExpense) {}
}
