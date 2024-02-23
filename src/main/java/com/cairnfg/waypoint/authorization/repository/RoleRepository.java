package com.cairnfg.waypoint.authorization.repository;

import com.cairnfg.waypoint.authorization.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findById(Long id);

  Optional<Role> findByName(String name);
}
