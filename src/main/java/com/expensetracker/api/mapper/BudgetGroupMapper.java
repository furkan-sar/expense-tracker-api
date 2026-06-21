package com.expensetracker.api.mapper;

import com.expensetracker.api.dto.BudgetGroupDetailResponse;
import com.expensetracker.api.dto.BudgetGroupResponse;
import com.expensetracker.api.dto.BudgetMemberResponse;
import com.expensetracker.api.entity.BudgetGroup;
import com.expensetracker.api.entity.BudgetMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface BudgetGroupMapper {

    @Mapping(target = "ownerUserId", source = "owner.id")
    @Mapping(target = "createdAt", expression = "java(toOffsetDateTime(budgetGroup.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(toOffsetDateTime(budgetGroup.getUpdatedAt()))")
    BudgetGroupResponse toResponse(BudgetGroup budgetGroup);

    @Mapping(target = "ownerUserId", source = "owner.id")
    @Mapping(target = "createdAt", expression = "java(toOffsetDateTime(budgetGroup.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(toOffsetDateTime(budgetGroup.getUpdatedAt()))")
    BudgetGroupDetailResponse toDetailResponse(BudgetGroup budgetGroup);

    @Mapping(target = "budgetGroupId", source = "budgetGroup.id")
    @Mapping(target = "joinedAt", expression = "java(toOffsetDateTime(budgetMember.getJoinedAt()))")
    BudgetMemberResponse toMemberResponse(BudgetMember budgetMember);

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
