package com.cairnfg.waypoint.authorization;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.RegisteredClient;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import com.cairnfg.waypoint.authorization.repository.PermissionRepository;
import com.cairnfg.waypoint.authorization.repository.RegisteredClientRepository;
import com.cairnfg.waypoint.authorization.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Slf4j
@SpringBootApplication
public class WaypointAuthorizationApiApp {

  @Autowired
  private AccountRepository accountRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private PermissionRepository permissionRepository;
  @Autowired
  private RegisteredClientRepository registeredClientRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(WaypointAuthorizationApiApp.class);
    app.setAdditionalProfiles("dev");
    app.run(args);
    log.info("App is running...");
  }

  @PostConstruct
  void init() {
    Account adminAccount = Account.builder()
        .firstName("test_name")
        .lastName("test_name")
        .username("test_admin")
        .email("no@no.com")
        .password(passwordEncoder.encode("password"))
        .roles(List.of(roleRepository.findByName("Admin").get()))
        .enabled(Boolean.TRUE)
        .address1("1600 Pennsylvania Avenue")
        .address2("Oval Office")
        .city("Washington")
        .state("District of Columbia")
        .zip("12345")
        .accountLocked(Boolean.FALSE)
        .accountExpirationDate(LocalDateTime.now().plusYears(10))
        .passwordExpirationDate(LocalDateTime.now().plusYears(10))
        .acceptedPA(Boolean.TRUE)
        .acceptedEULA(Boolean.TRUE)
        .acceptedTC(Boolean.TRUE)
        .build();

    try {
      accountRepository.save(adminAccount);
    } catch (Exception e) {
      //nothing to be done
    }

    Account userAccount = Account.builder()
        .firstName("test_name")
        .lastName("test_name")
        .username("test_user")
        .email("no@no.com")
        .password(passwordEncoder.encode("password"))
        .roles(List.of(roleRepository.findByName("User").get()))
        .enabled(Boolean.TRUE)
        .address1("1600 Pennsylvania Avenue")
        .address2("Oval Office")
        .city("Washington")
        .state("District of Columbia")
        .zip("12345")
        .accountLocked(Boolean.FALSE)
        .accountExpirationDate(LocalDateTime.now().plusYears(10))
        .passwordExpirationDate(LocalDateTime.now().plusYears(10))
        .acceptedPA(Boolean.TRUE)
        .acceptedEULA(Boolean.TRUE)
        .acceptedTC(Boolean.TRUE)
        .build();

    try {
      accountRepository.save(userAccount);
    } catch (Exception e) {
      //nothing to be done
    }

    RegisteredClient oidcClient = RegisteredClient.builder()
        .clientId("oidc-client")
        .clientIdIssuedAt(Instant.now())
        .clientSecret(passwordEncoder.encode("secret"))
        .clientSecretExpiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 365))
        .clientAuthenticationMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
        .authorizationGrantTypes(
            Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
        .redirectUris(Set.of("http://127.0.0.1:8082/login/oauth2/code/oidc-client"))
        .postLogoutRedirectUris(Set.of("http://127.0.0.1:8082/"))
        .scopes(Set.of(OidcScopes.OPENID, OidcScopes.PROFILE))
        .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
        .tokenSettings(TokenSettings.builder().build())
        .build();

    try {
      registeredClientRepository.save(oidcClient);
    } catch (Exception e) {
      //nothing to be done
    }
  }
}
