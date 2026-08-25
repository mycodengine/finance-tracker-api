package com.financetracker.mapper;

import com.financetracker.domain.entity.Account;
import com.financetracker.dto.response.AccountResponse;
import org.mapstruct.Mapper;

/** MapStruct mapper — generates implementation at compile time. */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toResponse(Account account);
}
