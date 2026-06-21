package com.expensetracker.api.controller;

import com.expensetracker.api.dto.BudgetMemberCreateRequest;
import com.expensetracker.api.dto.BudgetMemberResponse;
import com.expensetracker.api.security.AuthenticatedUser;
import com.expensetracker.api.service.BudgetMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/budget-groups/{id}/members")
@RequiredArgsConstructor
public class BudgetMemberController {

    private final BudgetMemberService budgetMemberService;

    @GetMapping
    public List<BudgetMemberResponse> listBudgetMembers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id
    ) {
        return budgetMemberService.listBudgetMembers(currentUser.user(), id);
    }

    @PostMapping
    public ResponseEntity<BudgetMemberResponse> addBudgetMember(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody BudgetMemberCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetMemberService.addBudgetMember(currentUser.user(), id, request));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeBudgetMember(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @PathVariable UUID memberId
    ) {
        budgetMemberService.removeBudgetMember(currentUser.user(), id, memberId);
        return ResponseEntity.noContent().build();
    }
}
