package com.expensetracker.api.controller;

import com.expensetracker.api.dto.CategoryReportItem;
import com.expensetracker.api.dto.DashboardReportResponse;
import com.expensetracker.api.dto.ExpenseTrendReportItem;
import com.expensetracker.api.dto.MemberSpendingReportItem;
import com.expensetracker.api.dto.MonthlySummaryReportItem;
import com.expensetracker.api.dto.SummaryReportResponse;
import com.expensetracker.api.dto.TransactionType;
import com.expensetracker.api.security.AuthenticatedUser;
import com.expensetracker.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    public SummaryReportResponse getSummaryReport(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID memberId,
            @RequestParam(required = false) TransactionType transactionType
    ) {
        return reportService.getSummaryReport(currentUser.user(), budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
    }

    @GetMapping("/categories")
    public List<CategoryReportItem> getCategoryReport(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID memberId,
            @RequestParam(required = false) TransactionType transactionType
    ) {
        return reportService.getCategoryReport(currentUser.user(), budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
    }

    @GetMapping("/trends")
    public List<ExpenseTrendReportItem> getExpenseTrendReport(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID memberId,
            @RequestParam(required = false) TransactionType transactionType
    ) {
        return reportService.getExpenseTrendReport(currentUser.user(), budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
    }

    @GetMapping("/monthly-summary")
    public List<MonthlySummaryReportItem> getMonthlySummaryReport(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID memberId,
            @RequestParam(required = false) TransactionType transactionType
    ) {
        return reportService.getMonthlySummaryReport(currentUser.user(), budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
    }

    @GetMapping("/members")
    public List<MemberSpendingReportItem> getMemberReport(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID memberId,
            @RequestParam(required = false) TransactionType transactionType
    ) {
        return reportService.getMemberReport(currentUser.user(), budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
    }

    @GetMapping("/dashboard")
    public DashboardReportResponse getDashboardReport(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID memberId,
            @RequestParam(required = false) TransactionType transactionType
    ) {
        return reportService.getDashboardReport(currentUser.user(), budgetGroupId, startDate, endDate, categoryId, memberId, transactionType);
    }
}
