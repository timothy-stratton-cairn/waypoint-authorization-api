package com.cairnfg.waypoint.authorization.controller.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2LoginEndpoint {
    public static final String PATH = "/api/oauth/token";

    @Autowired
    private AuthenticationManager authenticationManager;

    @PreAuthorize("permitAll()")
    @PostMapping(PATH)
    public ResponseEntity<Authentication> login() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("test_username", "password");
        authenticationManager.authenticate(authenticationToken);

        return ResponseEntity.ok(authenticationManager.authenticate(authenticationToken));
    }
}
