package com.cairnfg.waypoint.authorization.repository;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.stereotype.Component;

@Component
public class RegisteredClientRepository extends JdbcRegisteredClientRepository {
    public RegisteredClientRepository(JdbcOperations jdbcOperations) {
        super(jdbcOperations);
    }
}
