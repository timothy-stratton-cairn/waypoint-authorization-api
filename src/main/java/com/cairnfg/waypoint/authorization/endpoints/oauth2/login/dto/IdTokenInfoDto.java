package com.cairnfg.waypoint.authorization.endpoints.login.dto;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Data
@Builder
public class IdTokenInfoDto {

    private String username;
    private String firstName;
    private String email;
    private Boolean acceptedTC; //terms and conditions
    private Boolean acceptedEULA; //end-user licensing agreement
    private Boolean acceptedPA; //privacy agreement

    @JsonIgnore
    public static final IdTokenInfoDtoMapper MAPPER = IdTokenInfoDtoMapper.INSTANCE;

    @Mapper
    public interface IdTokenInfoDtoMapper {
        IdTokenInfoDtoMapper INSTANCE = Mappers.getMapper(IdTokenInfoDtoMapper.class);


        IdTokenInfoDto accountToIdTokenInfoDto(Account account);
    }
}
