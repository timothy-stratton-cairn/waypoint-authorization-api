package com.cairnfg.waypoint.authorization.repository;

import com.cairnfg.waypoint.authorization.entity.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationRepository extends JpaRepository<Authorization, Long> {

}
