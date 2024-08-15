package com.cairnfg.waypoint.authorization.service;


import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @InjectMocks
  private AccountService underTest;

  @Mock
  private AccountRepository accountRepository;

  @Test
  void givenExistingUsername_whenLoadUserByUsername_thenUserIsReturn() {
    //Given
    Account testAccount = Account.builder()
        .firstName("first-name")
        .lastName("last-name")
        .email("no@no.com")
        .username("username-1")
        .build();

    when(accountRepository.findByUsername(anyString())).thenReturn(Optional.of(testAccount));

    //When
    UserDetails returnedUserDetails = underTest.loadUserByUsername(testAccount.getUsername());

    //Then
    assertThat(returnedUserDetails)
        .isNotNull()
        .isInstanceOf(Account.class)
        .isEqualTo(testAccount);
  }

  @Test
  void givenNonexistentUsername_whenLoadUserByUsername_thenExceptionIsThrown() {
    //Given
    when(accountRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    //When - Then
    assertThatThrownBy(() -> underTest.loadUserByUsername("non-existent-username"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenExistingUsername_whenFindByUsername_thenUserIsReturn() {
    //Given
    Account testAccount = Account.builder()
        .firstName("first-name")
        .lastName("last-name")
        .email("no@no.com")
        .username("username-1")
        .build();

    when(accountRepository.findByUsername(anyString())).thenReturn(Optional.of(testAccount));

    //When
    Optional<Account> returnedAccount = underTest.findByUsername(testAccount.getUsername());

    //Then
    assertThat(returnedAccount)
        .isPresent()
        .get()
        .isEqualTo(testAccount);

  }

  @Test
  void givenNonexistentUsername_whenFindByUsername_thenEmptyOptionIsReturned() {
    //Given
    when(accountRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    //When
    Optional<Account> returnedAccount = underTest.findByUsername("non-existent-username");

    //Then
    assertThat(returnedAccount)
        .isEmpty()
        .isNotPresent();
  }
}