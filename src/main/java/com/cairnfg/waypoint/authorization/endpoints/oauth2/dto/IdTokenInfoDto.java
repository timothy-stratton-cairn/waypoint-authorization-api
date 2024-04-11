package com.cairnfg.waypoint.authorization.endpoints.oauth2.dto;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Data
@Builder
public class IdTokenInfoDto {

  @JsonIgnore
  public static final IdTokenInfoDtoMapper MAPPER = IdTokenInfoDtoMapper.INSTANCE;

  private Long accountId;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private Boolean acceptedTC; //terms and conditions
  private Boolean acceptedEULA; //end-user licensing agreement
  private Boolean acceptedPA; //privacy agreement

  @Mapper
  public interface IdTokenInfoDtoMapper {

    IdTokenInfoDtoMapper INSTANCE = Mappers.getMapper(IdTokenInfoDtoMapper.class);

    @Mapping(source = "id", target = "accountId")
    IdTokenInfoDto accountToIdTokenInfoDto(Account account);
  }
}
