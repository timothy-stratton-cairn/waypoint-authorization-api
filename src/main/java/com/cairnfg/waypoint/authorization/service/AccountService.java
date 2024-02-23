package com.cairnfg.waypoint.authorization.service;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username).orElseThrow(IllegalArgumentException::new);
    }

    public Optional<Account> findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
}
