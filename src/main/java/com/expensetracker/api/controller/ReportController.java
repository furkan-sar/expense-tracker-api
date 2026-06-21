package com.expensetracker.api.controller;

import com.expensetracker.api.dto.CategoryReportItem;
import com.expensetracker.api.dto.MemberReportItem;
import com.expensetracker.api.dto.SummaryReportResponse;
import com.expensetracker.api.dto.TransactionType;
import com.expensetracker.api.security.AuthenticatedUser;
import com.expensetracker.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return reportService.getSummaryReport(currentUser.user(), budgetGroupId, startDate, endDate);
    }

    @GetMapping("/categories")
    public List<CategoryReportItem> getCategoryReport(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type
    ) {
        return reportService.getCategoryReport(currentUser.user(), budgetGroupId, startDate, endDate, type);
    }

    @GetMapping("/members")
    public List<MemberReportItem> getMemberReport(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return reportService.getMemberReport(currentUser.user(), budgetGroupId, startDate, endDate);
    }
}
