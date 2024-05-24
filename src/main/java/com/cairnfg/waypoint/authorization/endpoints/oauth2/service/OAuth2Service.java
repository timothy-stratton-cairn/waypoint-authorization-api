package com.cairnfg.waypoint.authorization.endpoints.oauth2.service;

import com.cairnfg.waypoint.authorization.endpoints.oauth2.dto.IdTokenInfoDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Authorization;
import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.service.AuthorizationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.stereotype.Component;

@Component
public class OAuth2Service {

  private final AuthorizationService authorizationService;
  private final AuthorizationServerSettings authorizationServerSettings;
  private final RSAKey rsaKey;

  public OAuth2Service(AuthorizationService authorizationService,
      AuthorizationServerSettings authorizationServerSettings,
      RSAKey rsaKey) {
    this.authorizationService = authorizationService;
    this.authorizationServerSettings = authorizationServerSettings;
    this.rsaKey = rsaKey;
  }

  public Authorization generateOAuth2TokenFamily(Account account, Date expiresAt)
      throws JOSEException {
    String authorizationId = UUID.randomUUID().toString();
    String accessToken = generateJwt(generateAccessTokenClaimsSet(account, expiresAt));
    String refreshToken = generateJwt(
        generateRefreshTokenClaimsSet(account, authorizationId, expiresAt));
    String idToken = generateJwt(generateIdTokenClaimsSet(account, expiresAt));

    return saveAuthorizationToDatabase(accessToken, refreshToken, idToken, authorizationId,
        account);
  }

  public Authorization saveAuthorizationToDatabase(String accessToken, String refreshToken,
      String idToken,
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
        .subject(account.getUsername()).audience(
            Collections.singletonList(account.getUsername())) //audience should be RegisteredClient
        .claim("scope", account.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" ")))
        .claim("groups", account.getRoles().stream().map(Role::getName)
            .collect(Collectors.toList()))
        .expirationTime(expirationTime)
        .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now()))
        .jwtID(UUID.randomUUID().toString())
        .build();
  }

  public JWTClaimsSet generateRefreshTokenClaimsSet(Account account, String authorizationId,
      Date expirationTime) {
    return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
        .subject(account.getUsername()).audience(
            Collections.singletonList(account.getUsername())) //audience should be RegisteredClient
        .claim("authorizationId", authorizationId)
        .expirationTime(expirationTime)
        .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now()))
        .jwtID(UUID.randomUUID().toString())
        .build();
  }

  public JWTClaimsSet generateIdTokenClaimsSet(Account account, Date expirationTime) {
    return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
        .subject(account.getUsername()).audience(
            Collections.singletonList(account.getUsername())) //audience should be RegisteredClient
        .claim("idInfo", IdTokenInfoDto.builder()
            .accountId(account.getId())
            .username(account.getUsername())
            .firstName(account.getFirstName())
            .lastName(account.getLastName())
            .email(account.getEmail())
            .roles(account.getRoles().stream()
                .map(Role::getName)
                .toList())
            .householdId(account.getHousehold() != null ?
                account.getHousehold().getId() : null)
            .acceptedTC(account.getAcceptedTC())
            .acceptedEULA(account.getAcceptedEULA())
            .acceptedPA(account.getAcceptedPA())
            .build())
        .expirationTime(expirationTime)
        .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now()))
        .jwtID(UUID.randomUUID().toString())
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
