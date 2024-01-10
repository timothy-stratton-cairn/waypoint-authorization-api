package com.cairnfg.waypoint.authorization.repository;

import com.cairnfg.waypoint.authorization.entity.RegisteredClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;

public interface RegisteredClientRepository extends JpaRepository<RegisteredClient, String> {
}
