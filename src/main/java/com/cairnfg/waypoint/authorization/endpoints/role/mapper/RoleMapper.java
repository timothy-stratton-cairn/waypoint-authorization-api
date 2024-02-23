package com.cairnfg.waypoint.authorization.endpoints.role.mapper;

import com.cairnfg.waypoint.authorization.endpoints.role.dto.RoleDto;
import com.cairnfg.waypoint.authorization.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleMapper {

  RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

  RoleDto toDto(Role account);
}
