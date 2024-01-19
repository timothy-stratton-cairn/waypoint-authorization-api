package com.cairnfg.waypoint.authorization.controller.user;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GetUserEndpoint {
    public static final String PATH = "/api/user";

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping(PATH)
    public ResponseEntity<List<Account>> getAllUsers() {
        return ResponseEntity.ok(List.of(accountRepository.findByUsername("test_username").get()));
    }
}
