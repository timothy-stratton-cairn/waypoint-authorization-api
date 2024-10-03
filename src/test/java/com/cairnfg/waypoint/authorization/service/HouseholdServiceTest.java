package com.cairnfg.waypoint.authorization.service;


import com.cairnfg.waypoint.authorization.endpoints.household.dto.HouseholdAccountDetailsDto;
import com.cairnfg.waypoint.authorization.endpoints.household.dto.enumeration.HouseholdRoleEnum;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Household;
import com.cairnfg.waypoint.authorization.service.helper.HouseholdHelperService;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import com.cairnfg.waypoint.authorization.repository.HouseholdRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

  @InjectMocks
  private HouseholdService householdUnderTest;
  @InjectMocks
  private AccountService accUnderTest;

  @Mock
  private AccountRepository accountRepository;
  @Mock
  private HouseholdRepository householdRepository;
  
  @Mock
  private Account testAccount;

  @Mock
  private Household testHousehold;

  @Mock
  private HouseholdAccountDetailsDto lists;
  
  

  

  @Test
  void shouldReturnPrimaryContactRole_whenAccountIsPrimaryContact() {
      // Given
      Household testHousehold = Household.builder()
          .id(1L)
          .name("Test Household")
          .householdAccounts(new HashSet<>())
          .build();

      Account testAccount = Account.builder()
          .username("username-1")
          .firstName("first-name")
          .lastName("last-name")
          .email("no@no.com")
          .isPrimaryContactForHousehold(true)
          .household(testHousehold)
          .build();

      // Add the account to the household's accounts set
      testHousehold.setHouseholdAccounts(Set.of(testAccount));

      // When
      HouseholdRoleEnum result = HouseholdHelperService.getHouseholdRole(testHousehold, testAccount);

      // Then
      assertThat(result).isEqualTo(HouseholdRoleEnum.PRIMARY_CONTACT);
  }
  
  
  @Test
  void shouldReturnCoClientRole_whenAccountIsCoClient() {
      // Given
      Household testHousehold = Household.builder()
          .id(2L)
          .name("Another Test Household")
          .householdAccounts(new HashSet<>())
          .build();

      Account coClient = Account.builder()
    		  .id(2L)
    		  .username("username-2")
              .firstName("first-name")
              .lastName("last-name")
              .email("no@no.com")
              .isPrimaryContactForHousehold(false)
              .coClient(null)
              .household(testHousehold)
              .build();
      
      Account primaryContact = Account.builder()
              .username("primary-username")
              .firstName("Primary")
              .lastName("Contact")
              .isPrimaryContactForHousehold(true)
              .household(testHousehold)
              .build();

      Account testAccount = Account.builder()
    	  .id(4L)
          .username("username-2")
          .firstName("first-name")
          .lastName("last-name")
          .email("no@no.com")
          .isPrimaryContactForHousehold(false)
          .coClient(coClient) // Setting the co-client
          .household(testHousehold)
          .build();

      // When
      HouseholdRoleEnum result = HouseholdHelperService.getHouseholdRole(testHousehold, testAccount);

      // Then
      assertThat(result).isEqualTo(HouseholdRoleEnum.CO_CLIENT);
  }

  @Test
  void shouldReturnDependentRole_whenAccountIsDependent() {
      // Given
      Household testHousehold = Household.builder()
          .id(3L)
          .name("Dependent Household")
          .householdAccounts(new HashSet<>())
          .build();

      Account testAccount = Account.builder()
          .username("dependent-username")
          .firstName("Dependent")
          .lastName("Test")
          .isPrimaryContactForHousehold(false)
          .coClient(null) // No co-client
          .household(testHousehold)
          .build();

      // When
      HouseholdRoleEnum result = HouseholdHelperService.getHouseholdRole(testHousehold, testAccount);

      // Then
      assertThat(result).isEqualTo(HouseholdRoleEnum.DEPENDENT);
  }
  
  @Test
  void shouldReturnPrimaryContactRole_whenAccountIsPrimaryContactAndHasMultipleAccounts() {
      // Given
      Household testHousehold = Household.builder()
          .id(2L)
          .name("Test Household with Primary Contact")
          .householdAccounts(new HashSet<>())
          .build();

      Account primaryContact = Account.builder()
          .username("primary-username")
          .firstName("Primary")
          .lastName("Contact")
          .isPrimaryContactForHousehold(true)
          .household(testHousehold)
          .build();

      Account otherAccount = Account.builder()
          .username("other-username")
          .firstName("Other")
          .lastName("Account")
          .isPrimaryContactForHousehold(false)
          .household(testHousehold)
          .build();

      // Add both accounts to the household
      testHousehold.setHouseholdAccounts(Set.of(primaryContact, otherAccount));

      // When
      HouseholdRoleEnum result = HouseholdHelperService.getHouseholdRole(testHousehold, primaryContact);

      // Then
      assertThat(result).isEqualTo(HouseholdRoleEnum.PRIMARY_CONTACT);
  }

  @Test
  void shouldReturnCoClientRole_whenAccountIsInHouseholdButNotPrimaryContact() {
      // Given
      Household testHousehold = Household.builder()
          .id(3L)
          .name("Test Household with Multiple Accounts")
          .householdAccounts(new HashSet<>())
          .build();

      Account primaryContact = Account.builder()
          .username("primary-username")
          .firstName("Primary")
          .lastName("Contact")
          .isPrimaryContactForHousehold(true)
          .household(testHousehold)
          .build();

      Account coClient = Account.builder()
          .username("co-client-username")
          .firstName("CoClient")
          .lastName("Test")
          .isPrimaryContactForHousehold(false)
          .household(testHousehold)
          .coClient(primaryContact)
          .build();

      // Add both accounts to the household
      testHousehold.setHouseholdAccounts(Set.of(primaryContact, coClient));

      // When
      HouseholdRoleEnum result = HouseholdHelperService.getHouseholdRole(testHousehold, coClient);

      // Then
      assertThat(result).isEqualTo(HouseholdRoleEnum.CO_CLIENT);
  }

  @Test
  void shouldReturnDependentRole_whenAccountIsNotInHousehold() {
      // Given
      Household testHousehold = Household.builder()
          .id(4L)
          .name("Test Household with No Accounts")
          .primaryContacts(null)
          .householdAccounts(new HashSet<>())
          .build();

      Account testAccount = Account.builder()
          .username("dependent-username")
          .firstName("Dependent")
          .lastName("Account")
          .isPrimaryContactForHousehold(false)
          .build();

      // When
      HouseholdRoleEnum result = HouseholdHelperService.getHouseholdRole(testHousehold, testAccount);

      // Then
      assertThat(result).isEqualTo(HouseholdRoleEnum.DEPENDENT);
  }

  
  @Test
  void shouldThrowException_whenAccountIsNull() {
      // Given
      Household testHousehold = Household.builder()
          .id(1L)
          .name("Test Household")
          .householdAccounts(new HashSet<>())
          .build();

      // Expect an exception when account is null
      assertThatThrownBy(() -> HouseholdHelperService.getHouseholdRole(null, testAccount))
      .isInstanceOf(Exception.class);
  }

  @Test
  void shouldThrowException_whenHouseholdIsNull() {
      // Given
      Account testAccount = Account.builder()
          .username("null-household-account")
          .firstName("Test")
          .lastName("Account")
          .isPrimaryContactForHousehold(false)
          .build();

      // Expect an exception when household is null
      assertThatThrownBy(() -> HouseholdHelperService.getHouseholdRole(null, testAccount))
      .isInstanceOf(Exception.class);
  }

  @Test
  void shouldThrowException_whenBothHouseholdAndAccountAreNull() {
      // Expect an exception when both are null
	  assertThatThrownBy(() -> HouseholdHelperService.getHouseholdRole(null, testAccount))
      .isInstanceOf(Exception.class);
  }
  
  
  
}
  
