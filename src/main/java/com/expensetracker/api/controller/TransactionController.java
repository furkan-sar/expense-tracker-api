package com.expensetracker.api.controller;

import com.expensetracker.api.dto.TransactionCreateRequest;
import com.expensetracker.api.dto.TransactionPageResponse;
import com.expensetracker.api.dto.TransactionResponse;
import com.expensetracker.api.dto.TransactionType;
import com.expensetracker.api.dto.TransactionUpdateRequest;
import com.expensetracker.api.security.AuthenticatedUser;
import com.expensetracker.api.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public TransactionPageResponse listTransactions(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID budgetGroupId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return transactionService.listTransactions(currentUser.user(), budgetGroupId, categoryId, type, startDate, endDate, page, size);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody TransactionCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(currentUser.user(), request));
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        return transactionService.getTransaction(currentUser.user(), id);
    }

    @PutMapping("/{id}")
    public TransactionResponse updateTransaction(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionUpdateRequest request
    ) {
        return transactionService.updateTransaction(currentUser.user(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        transactionService.deleteTransaction(currentUser.user(), id);
        return ResponseEntity.noContent().build();
    }
}
