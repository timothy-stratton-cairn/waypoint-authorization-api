package com.cairnfg.waypoint.authorization.service;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements UserDetailsService {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return this.accountRepository.findByUsername(username)
        .orElseThrow(IllegalArgumentException::new);
  }

  public Optional<Account> findByUsername(String username) {
    return this.accountRepository.findByUsername(username);
  }

  public List<Account> getAllAccounts() {
    return this.accountRepository.findAll();
  }

  public Long createAccount(Account account) {
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    account.setCreated(LocalDateTime.now());
    account.setUpdated((LocalDateTime.now()));
    account.setAccountExpirationDate(LocalDateTime.now().plusYears(5)); //account expires in 5 years
    account.setPasswordExpirationDate(
        LocalDateTime.now().plusYears(1)); //password expires in 1 year
    account.setEnabled(Boolean.TRUE);
    account.setAccountLocked(Boolean.FALSE);
    account.setAcceptedTC(Boolean.FALSE);
    account.setAcceptedEULA(Boolean.FALSE);
    account.setAcceptedPA(Boolean.FALSE);

    return this.accountRepository.save(account).getId();
  }
}
