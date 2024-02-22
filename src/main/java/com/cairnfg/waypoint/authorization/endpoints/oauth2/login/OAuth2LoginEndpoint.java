package com.cairnfg.waypoint.authorization.endpoints.oauth2.login;

import com.cairnfg.waypoint.authorization.endpoints.login.dto.IdTokenInfoDto;
import com.cairnfg.waypoint.authorization.endpoints.login.dto.LoginRequestDto;
import com.cairnfg.waypoint.authorization.endpoints.login.dto.SuccessfulLoginResponseDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Authorization;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import com.cairnfg.waypoint.authorization.service.AuthorizationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class OAuth2LoginEndpoint {
    public static final String PATH = "/api/oauth/token";

    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final AuthorizationService authorizationService;
    private final AuthorizationServerSettings authorizationServerSettings;
    private final RSAKey rsaKey;

    public OAuth2LoginEndpoint(AuthenticationManager authenticationManager, AccountRepository accountRepository,
                               AuthorizationService authorizationService, AuthorizationServerSettings authorizationServerSettings,
                               RSAKey rsaKey) {
        this.authenticationManager = authenticationManager;
        this.accountRepository = accountRepository;
        this.authorizationService = authorizationService;
        this.authorizationServerSettings = authorizationServerSettings;
        this.rsaKey = rsaKey;
    }

    @PreAuthorize("permitAll()")
    @PostMapping(PATH)
    public ResponseEntity<SuccessfulLoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) throws JOSEException {
        log.info("Attempting to login to user account with username [{}]", loginRequest.getUsername());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        if (this.authenticationManager.authenticate(authenticationToken).isAuthenticated()) {
            Account account = this.accountRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(EntityNotFoundException::new);
            Date expiresAt = Date.from(Instant.now().plusSeconds(3600));

            Authorization authorization = generateOAuth2TokenFamily(account, expiresAt);

            return generateSuccessResponse(authorization, expiresAt);
        } else {
            log.info("Login attempt to user account with username [{}] failed", loginRequest.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    public Authorization generateOAuth2TokenFamily(Account account, Date expiresAt) throws JOSEException {
        String authorizationId = UUID.randomUUID().toString();
        String accessToken = generateJwt(generateAccessTokenClaimsSet(account, expiresAt));
        String refreshToken = generateJwt(generateRefreshTokenClaimsSet(account, authorizationId, expiresAt));
        String idToken = generateJwt(generateIdTokenClaimsSet(account, expiresAt));

        return saveAuthorizationToDatabase(accessToken, refreshToken, idToken, authorizationId, account);
    }

    public ResponseEntity<SuccessfulLoginResponseDto> generateSuccessResponse(Authorization authorization, Date expiresAt) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        SuccessfulLoginResponseDto responseDto = SuccessfulLoginResponseDto.builder()
                .accessToken(authorization.getAccessToken())
                .refreshToken(authorization.getRefreshToken())
                .idToken(authorization.getIdToken())
                .expiresAt(formatter.format(expiresAt))
                .permissions(authorization.getAccount()
                        .getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .build();

        return ResponseEntity.ok(responseDto);
    }

    public Authorization saveAuthorizationToDatabase(String accessToken, String refreshToken, String idToken,
                                                     String authorizationId, Account account) {
        Authorization authorization = Authorization.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .idToken(idToken)
                .authorizationGuid(authorizationId)
                .account(account)
                .build();

        return this.authorizationService.saveAuthorization(authorization);
    }

    public JWTClaimsSet generateAccessTokenClaimsSet(Account account, Date expirationTime) {
        return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
                .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername())) //audience should be RegisteredClient
                .claim("scope", account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" ")))
                .claim("groups", account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .expirationTime(expirationTime)
                .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now())).jwtID(UUID.randomUUID().toString())
                .build();
    }

    public JWTClaimsSet generateRefreshTokenClaimsSet(Account account, String authorizationId, Date expirationTime) {
        return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
                .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername())) //audience should be RegisteredClient
                .claim("authorizationId", authorizationId)
                .expirationTime(expirationTime)
                .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now())).jwtID(UUID.randomUUID().toString())
                .build();
    }

    public JWTClaimsSet generateIdTokenClaimsSet(Account account, Date expirationTime) {
        return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
                .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername())) //audience should be RegisteredClient
                .claim("idInfo", IdTokenInfoDto.MAPPER.accountToIdTokenInfoDto(account))
                .expirationTime(expirationTime)
                .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now())).jwtID(UUID.randomUUID().toString())
                .build();
    }

    protected String generateJwt(JWTClaimsSet claimsSet)
            throws JOSEException {
        SignedJWT signedJWT;

        JWSSigner signer = new RSASSASigner(this.rsaKey);
        signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}
