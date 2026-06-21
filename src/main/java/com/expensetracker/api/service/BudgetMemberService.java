package com.expensetracker.api.service;

import com.expensetracker.api.dto.BudgetMemberCreateRequest;
import com.expensetracker.api.dto.BudgetMemberResponse;
import com.expensetracker.api.entity.BudgetGroup;
import com.expensetracker.api.entity.BudgetMember;
import com.expensetracker.api.entity.BudgetMemberRole;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.exception.AuthorizationException;
import com.expensetracker.api.exception.ConflictException;
import com.expensetracker.api.exception.NotFoundException;
import com.expensetracker.api.mapper.BudgetGroupMapper;
import com.expensetracker.api.repository.BudgetGroupRepository;
import com.expensetracker.api.repository.BudgetMemberRepository;
import com.expensetracker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetMemberService {

    private final BudgetGroupRepository budgetGroupRepository;
    private final BudgetMemberRepository budgetMemberRepository;
    private final UserRepository userRepository;
    private final BudgetGroupMapper budgetGroupMapper;

    @Transactional(readOnly = true)
    public List<BudgetMemberResponse> listBudgetMembers(User currentUser, UUID budgetGroupId) {
        BudgetGroup budgetGroup = requireBudgetGroup(budgetGroupId);
        requireMemberAccess(budgetGroup, currentUser);

        return budgetMemberRepository.findAllByBudgetGroupIdWithUser(budgetGroupId)
                .stream()
                .map(budgetGroupMapper::toMemberResponse)
                .toList();
    }

    @Transactional
    public BudgetMemberResponse addBudgetMember(User currentUser, UUID budgetGroupId, BudgetMemberCreateRequest request) {
        BudgetGroup budgetGroup = requireBudgetGroup(budgetGroupId);
        requireManagerAccess(budgetGroup, currentUser);

        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new NotFoundException("User was not found."));

        if (budgetMemberRepository.existsByBudgetGroupIdAndUserId(budgetGroupId, user.getId())) {
            throw new ConflictException("User is already a budget group member.");
        }

        BudgetMember member = new BudgetMember();
        member.setBudgetGroup(budgetGroup);
        member.setUser(user);
        member.setRole(toEntityRole(request.role()));

        return budgetGroupMapper.toMemberResponse(budgetMemberRepository.save(member));
    }

    @Transactional
    public void removeBudgetMember(User currentUser, UUID budgetGroupId, UUID memberId) {
        BudgetGroup budgetGroup = requireBudgetGroup(budgetGroupId);
        requireManagerAccess(budgetGroup, currentUser);

        BudgetMember member = budgetMemberRepository.findByIdAndBudgetGroupId(memberId, budgetGroupId)
                .orElseThrow(() -> new NotFoundException("Budget member was not found."));

        if (member.getRole() == BudgetMemberRole.OWNER) {
            throw new AuthorizationException("Authenticated user is not allowed to access this resource.");
        }

        budgetMemberRepository.delete(member);
    }

    private BudgetGroup requireBudgetGroup(UUID budgetGroupId) {
        return budgetGroupRepository.findById(budgetGroupId)
                .orElseThrow(() -> new NotFoundException("Budget group was not found."));
    }

    private void requireMemberAccess(BudgetGroup budgetGroup, User currentUser) {
        if (!budgetMemberRepository.existsByBudgetGroupIdAndUserId(budgetGroup.getId(), currentUser.getId())) {
            throw new AuthorizationException("Authenticated user is not allowed to access this resource.");
        }
    }

    private void requireManagerAccess(BudgetGroup budgetGroup, User currentUser) {
        BudgetMember member = budgetMemberRepository.findByBudgetGroupIdAndUserId(budgetGroup.getId(), currentUser.getId())
                .orElseThrow(() -> new AuthorizationException("Authenticated user is not allowed to access this resource."));

        if (member.getRole() != BudgetMemberRole.OWNER && member.getRole() != BudgetMemberRole.ADMIN) {
            throw new AuthorizationException("Authenticated user is not allowed to access this resource.");
        }
    }

    private BudgetMemberRole toEntityRole(com.expensetracker.api.dto.BudgetMemberRole role) {
        return BudgetMemberRole.valueOf(role.name());
    }
}
