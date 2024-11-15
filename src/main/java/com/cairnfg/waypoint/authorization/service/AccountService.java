package com.cairnfg.waypoint.authorization.service;

import com.cairnfg.waypoint.authorization.dto.LinksDto;
import com.cairnfg.waypoint.authorization.dto.MailRequestQueueDto;
import com.cairnfg.waypoint.authorization.dto.enumeration.MailRequestEnum;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import com.cairnfg.waypoint.authorization.utility.PasswordUtility;
import com.cairnfg.waypoint.authorization.utility.sqs.SqsUtility;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountService implements UserDetailsService {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final SqsUtility sqsUtility;

  @Value("${waypoint.authorization.reset-password.link}")
  private String passwordResetLink;

  public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
      SqsUtility sqsUtility) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
    this.sqsUtility = sqsUtility;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return this.accountRepository.findByUsername(username)
        .orElseThrow(IllegalArgumentException::new);
  }

  public Optional<Account> findByUsername(String username) {
    return this.accountRepository.findByUsername(username);
  }

  public Optional<Account> findByEmail(String email) {
    return this.accountRepository.findByEmail(email);
  }

  public Optional<Account> getAccountById(Long accountId) {
    return this.accountRepository.findById(accountId);
  }

  public List<Account> getAllAccounts() {
    return this.accountRepository.findAll().stream().distinct().collect(Collectors.toList());
  }

  public List<Account> getAccountListsByIdList(List<Long> ids) {
    return this.accountRepository.findAllById(ids).stream().distinct().collect(Collectors.toList());
  }

  public Account saveAccount(Account account) {
    return this.accountRepository.save(account);
  }

  public Collection<Account> saveAllAccounts(Collection<Account> accounts) {
    return this.accountRepository.saveAll(accounts);
  }

  public Account createAccount(Account account) {
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    account.setCreated(LocalDateTime.now());
    account.setUpdated((LocalDateTime.now()));
    account.setAccountExpirationDate(LocalDateTime.now().plusYears(5)); //account expires in 5 years
    account.setPasswordExpirationDate(
        LocalDateTime.now().plusYears(1)); //password expires in 1 year
    account.setActive(Boolean.TRUE);
    account.setAccountLocked(Boolean.FALSE);
    account.setAcceptedTC(Boolean.FALSE);
    account.setAcceptedEULA(Boolean.FALSE);
    account.setAcceptedPA(Boolean.FALSE);

    return this.accountRepository.save(account);
  }

  public void resetPassword(Account account) throws JsonProcessingException {
    account.setPasswordResetToken(PasswordUtility.generateRandomAlphanumericString());
    account.setPasswordResetTimestamp(LocalDateTime.now());

    sqsUtility.sendMessage(MailRequestQueueDto.builder()
        .requestType(MailRequestEnum.PASSWORD_RESET)
        .recipient(account.getEmail())
        .links(LinksDto.builder()
            .resetPasswordLink(passwordResetLink
                .replace("{passwordResetToken}", account.getPasswordResetToken())
                .replace("{username}", account.getUsername()))
            .build())
        .build());

    account.setPasswordResetToken(passwordEncoder.encode(account.getPasswordResetToken()));

    this.accountRepository.save(account);
    log.info("Password Reset email successfully sent for account with ID [{}]", account.getId());
  }

  @Transactional
  public void removeCoClientFromHousehold(Long accountId) {
    Optional<Account> accountOptional = accountRepository.findById(accountId);
    if (accountOptional.isEmpty()) {
      throw new IllegalArgumentException("Account with ID [" + accountId + "] not found");
    }

    Account account = accountOptional.get();

    if (Boolean.TRUE.equals(account.getIsPrimaryContactForHousehold())) {
      throw new IllegalStateException("Account with ID [" + accountId + "] is the primary contact and cannot be removed from household");
    }
    account.setHousehold(null);
    accountRepository.save(account);
    log.info("Account with ID [{}] removed from household successfully", accountId);
  }
}
