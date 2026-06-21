package com.expensetracker.api.service;

import com.expensetracker.api.dto.BudgetGroupCreateRequest;
import com.expensetracker.api.dto.BudgetGroupDetailResponse;
import com.expensetracker.api.dto.BudgetGroupResponse;
import com.expensetracker.api.dto.BudgetGroupUpdateRequest;
import com.expensetracker.api.dto.BudgetMemberRole;
import com.expensetracker.api.entity.BudgetGroup;
import com.expensetracker.api.entity.BudgetMember;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.exception.AuthorizationException;
import com.expensetracker.api.exception.NotFoundException;
import com.expensetracker.api.mapper.BudgetGroupMapper;
import com.expensetracker.api.repository.BudgetGroupRepository;
import com.expensetracker.api.repository.BudgetMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetGroupService {

    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetMemberRepository budgetMemberRepository;
    private final BudgetGroupMapper budgetGroupMapper;

    @Transactional(readOnly = true)
    public List<BudgetGroupResponse> listBudgetGroups(User currentUser) {
        return budgetGroupRepository.findAllByMemberUserId(currentUser.getId())
                .stream()
                .map(budgetGroupMapper::toResponse)
                .toList();
    }

    @Transactional
    public BudgetGroupResponse createBudgetGroup(User currentUser, BudgetGroupCreateRequest request) {
        BudgetGroup budgetGroup = new BudgetGroup();
        budgetGroup.setName(request.name().trim());
        budgetGroup.setDescription(normalizeNullable(request.description()));
        budgetGroup.setCurrency(request.currency());
        budgetGroup.setOwner(currentUser);

        BudgetMember ownerMember = new BudgetMember();
        ownerMember.setBudgetGroup(budgetGroup);
        ownerMember.setUser(currentUser);
        ownerMember.setRole(BudgetMemberRole.OWNER);
        budgetGroup.getMembers().add(ownerMember);

        return budgetGroupMapper.toResponse(budgetGroupRepository.save(budgetGroup));
    }

    @Transactional(readOnly = true)
    public BudgetGroupDetailResponse getBudgetGroup(User currentUser, UUID id) {
        BudgetGroup budgetGroup = requireBudgetGroup(id);
        requireMemberAccess(budgetGroup, currentUser);
        return budgetGroupMapper.toDetailResponse(budgetGroup);
    }

    @Transactional
    public BudgetGroupResponse updateBudgetGroup(User currentUser, UUID id, BudgetGroupUpdateRequest request) {
        BudgetGroup budgetGroup = requireBudgetGroup(id);
        requireOwnerAccess(budgetGroup, currentUser);

        budgetGroup.setName(request.name().trim());
        budgetGroup.setDescription(normalizeNullable(request.description()));
        budgetGroup.setCurrency(request.currency());

        return budgetGroupMapper.toResponse(budgetGroup);
    }

    @Transactional
    public void deleteBudgetGroup(User currentUser, UUID id) {
        BudgetGroup budgetGroup = requireBudgetGroup(id);
        requireOwnerAccess(budgetGroup, currentUser);
        budgetGroupRepository.delete(budgetGroup);
    }

    private BudgetGroup requireBudgetGroup(UUID id) {
        return budgetGroupRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new NotFoundException("Budget group was not found."));
    }

    private void requireMemberAccess(BudgetGroup budgetGroup, User currentUser) {
        if (!budgetMemberRepository.existsByBudgetGroupIdAndUserId(budgetGroup.getId(), currentUser.getId())) {
            throw new AuthorizationException("Authenticated user is not allowed to access this resource.");
        }
    }

    private void requireOwnerAccess(BudgetGroup budgetGroup, User currentUser) {
        if (!budgetGroup.getOwner().getId().equals(currentUser.getId())) {
            throw new AuthorizationException("Authenticated user is not allowed to access this resource.");
        }
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
