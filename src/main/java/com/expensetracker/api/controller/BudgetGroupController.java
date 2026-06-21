package com.expensetracker.api.controller;

import com.expensetracker.api.dto.BudgetGroupCreateRequest;
import com.expensetracker.api.dto.BudgetGroupDetailResponse;
import com.expensetracker.api.dto.BudgetGroupResponse;
import com.expensetracker.api.dto.BudgetGroupUpdateRequest;
import com.expensetracker.api.security.AuthenticatedUser;
import com.expensetracker.api.service.BudgetGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/budget-groups")
@RequiredArgsConstructor
public class BudgetGroupController {

    private final BudgetGroupService budgetGroupService;

    @GetMapping
    public List<BudgetGroupResponse> listBudgetGroups(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return budgetGroupService.listBudgetGroups(currentUser.user());
    }

    @PostMapping
    public ResponseEntity<BudgetGroupResponse> createBudgetGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody BudgetGroupCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetGroupService.createBudgetGroup(currentUser.user(), request));
    }

    @GetMapping("/{id}")
    public BudgetGroupDetailResponse getBudgetGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        return budgetGroupService.getBudgetGroup(currentUser.user(), id);
    }

    @PutMapping("/{id}")
    public BudgetGroupResponse updateBudgetGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody BudgetGroupUpdateRequest request
    ) {
        return budgetGroupService.updateBudgetGroup(currentUser.user(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        budgetGroupService.deleteBudgetGroup(currentUser.user(), id);
        return ResponseEntity.noContent().build();
    }
}
