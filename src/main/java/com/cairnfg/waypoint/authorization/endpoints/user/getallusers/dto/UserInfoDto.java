package com.cairnfg.waypoint.authorization.endpoints.user.getallusers.dto;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Data
@Builder
public class UserInfoDto {

    private String username;
    private String firstName;
    private String email;
    private Boolean acceptedTC; //terms and conditions
    private Boolean acceptedEULA; //end-user licensing agreement
    private Boolean acceptedPA; //privacy agreement

    @JsonIgnore
    public static final UserInfoDto.UserInfoDtoMapper MAPPER = UserInfoDto.UserInfoDtoMapper.INSTANCE;

    @Mapper
    public interface UserInfoDtoMapper {
        UserInfoDto.UserInfoDtoMapper INSTANCE = Mappers.getMapper(UserInfoDto.UserInfoDtoMapper.class);


        UserInfoDto accountToUserInfoDto(Account account);
    }
}
