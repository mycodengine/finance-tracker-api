package com.financetracker.mapper;

import com.financetracker.domain.entity.Transaction;
import com.financetracker.dto.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "accountId",   source = "account.id")
    @Mapping(target = "accountName", source = "account.name")
    @Mapping(target = "categoryId",  source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    TransactionResponse toResponse(Transaction transaction);
}
