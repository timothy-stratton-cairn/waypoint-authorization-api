package com.cairnfg.waypoint.authorization;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Permission;
import com.cairnfg.waypoint.authorization.entity.RegisteredClient;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import com.cairnfg.waypoint.authorization.repository.PermissionRepository;
import com.cairnfg.waypoint.authorization.repository.RegisteredClientRepository;
import com.cairnfg.waypoint.authorization.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
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

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
        app.setAdditionalProfiles("default");
        app.run(args);
        log.info("App is running...");
    }

    @PostConstruct
    void init() {

        Account account = Account.builder()
                .firstName("test_name")
                .lastName("test_name")
                .username("test_username")
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
                .accountExpired(Boolean.FALSE)
                .credentialsExpired(Boolean.FALSE)
                .acceptedPA(Boolean.TRUE)
                .acceptedEULA(Boolean.TRUE)
                .acceptedTC(Boolean.TRUE)
                .build();

        accountRepository.save(account);

        RegisteredClient oidcClient = RegisteredClient.builder()
                .clientId("oidc-client")
                .clientIdIssuedAt(Instant.now())
                .clientSecret(passwordEncoder.encode("secret"))
                .clientSecretExpiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 365))
                .clientAuthenticationMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .authorizationGrantTypes(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
                .redirectUris(Set.of("http://127.0.0.1:8082/login/oauth2/code/oidc-client"))
                .postLogoutRedirectUris(Set.of("http://127.0.0.1:8082/"))
                .scopes(Set.of(OidcScopes.OPENID, OidcScopes.PROFILE))
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .tokenSettings(TokenSettings.builder().build())
                .build();

        registeredClientRepository.save(oidcClient);
    }
}
