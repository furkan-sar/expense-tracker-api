package com.expensetracker.api.mapper;

import com.expensetracker.api.dto.CategoryResponse;
import com.expensetracker.api.dto.TransactionResponse;
import com.expensetracker.api.entity.Category;
import com.expensetracker.api.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", uses = BudgetGroupMapper.class)
public interface TransactionMapper {

    @Mapping(target = "budgetGroupId", source = "budgetGroup.id")
    @Mapping(target = "createdAt", expression = "java(toOffsetDateTime(transaction.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(toOffsetDateTime(transaction.getUpdatedAt()))")
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "budgetGroupId", source = "budgetGroup.id")
    @Mapping(target = "createdAt", expression = "java(toOffsetDateTime(category.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(toOffsetDateTime(category.getUpdatedAt()))")
    CategoryResponse toCategoryResponse(Category category);

    default com.expensetracker.api.dto.TransactionType toDtoType(com.expensetracker.api.entity.TransactionType type) {
        return type == null ? null : com.expensetracker.api.dto.TransactionType.valueOf(type.name());
    }

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
