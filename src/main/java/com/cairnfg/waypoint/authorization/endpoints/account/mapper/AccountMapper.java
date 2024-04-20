package com.cairnfg.waypoint.authorization.endpoints.account.mapper;

import com.cairnfg.waypoint.authorization.endpoints.account.dto.AccountDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.AddAccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.BatchAddAccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.account.dto.LinkedAccountDetailsDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper {

  AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

  AccountDto toDto(Account account);

  @Mapping(target = "roles", ignore = true)
  Account accountDetailsDtoToEntity(AddAccountDetailsDto userDetailsDto);

  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "password", ignore = true)
  Account accountDetailsDtoToEntity(BatchAddAccountDetailsDto userDetailsDto);

  LinkedAccountDetailsDto accountToLinkedAccountDetailsDto(Account account);
}
