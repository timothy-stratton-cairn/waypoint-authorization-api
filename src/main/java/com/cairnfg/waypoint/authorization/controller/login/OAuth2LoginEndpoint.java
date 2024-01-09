package com.cairnfg.waypoint.authorization.controller.login;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuth2LoginEndpoint {
    public static final String PATH = "/api/oauth/token";

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AccountRepository accountRepository;

    @PreAuthorize("permitAll()")
    @PostMapping(PATH)
    public ResponseEntity<Object> login() {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("test_username", "password");
        if (authenticationManager.authenticate(authenticationToken).isAuthenticated()) {
            Account account = accountRepository.findByUsername("test_username").get();
            return ResponseEntity.ok(account);
        }

        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
}
