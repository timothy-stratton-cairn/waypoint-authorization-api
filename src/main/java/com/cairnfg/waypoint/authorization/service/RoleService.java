package com.cairnfg.waypoint.authorization.service;

import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

  private final RoleRepository roleRepository;

  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public List<Role> getAllRoles() {
    return this.roleRepository.findAll();
  }

  public List<Role> getAllRoles(List<Long> roleIds) {
    return this.roleRepository.findAllById(roleIds);
  }

  public Set<Role> getAccountRoleMappingDetails(Set<Long> roleIdList) {
    return roleIdList.stream()
        .map(roleId -> this.roleRepository.findById(roleId)
            .orElseThrow(
                () -> new EntityNotFoundException("Role with ID [" + roleId + "] not found")))
        .collect(Collectors.toSet());
  }

  public Set<Role> getRolesByName(Set<String> roleNamesList) {
    return roleNamesList.stream()
        .map(roleName -> this.roleRepository.findByName(roleName.toUpperCase())
            .orElseThrow(
                () -> new EntityNotFoundException("Role with Name [" + roleName + "] not found")))
        .collect(Collectors.toSet());
  }
}
