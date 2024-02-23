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

  public Set<Role> getAccountRoleMappingDetails(Set<Long> roleIdList) {
    return roleIdList.stream()
        .map(roleId -> this.roleRepository.findById(roleId)
            .orElseThrow(
                () -> new EntityNotFoundException("Role with ID [" + roleId + "] not found")))
        .collect(Collectors.toSet());
  }
}
