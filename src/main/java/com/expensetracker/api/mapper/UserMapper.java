package com.expensetracker.api.mapper;

import com.expensetracker.api.dto.UserResponse;
import com.expensetracker.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "createdAt", expression = "java(toOffsetDateTime(user.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(toOffsetDateTime(user.getUpdatedAt()))")
    UserResponse toResponse(User user);

    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
