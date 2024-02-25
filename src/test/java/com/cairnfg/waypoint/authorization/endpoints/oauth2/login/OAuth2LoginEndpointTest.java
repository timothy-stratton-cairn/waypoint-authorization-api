package com.cairnfg.waypoint.authorization.endpoints.oauth2.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cairnfg.waypoint.authorization.configuration.cryptography.RsaConfiguration;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.OAuth2LoginEndpoint;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.dto.IdTokenInfoDto;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.dto.LoginRequestDto;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.dto.SuccessfulLoginResponseDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Authorization;
import com.cairnfg.waypoint.authorization.entity.Permission;
import com.cairnfg.waypoint.authorization.entity.Role;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import com.cairnfg.waypoint.authorization.service.AuthorizationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class OAuth2LoginEndpointTest {

  public static final String ISSUER = "http://localhost:9999";

  @InjectMocks
  private OAuth2LoginEndpoint underTest;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private AuthorizationService authorizationService;

  @Mock
  private AuthorizationServerSettings authorizationServerSettings;

  @Mock
  private RSAKey rsaKey;

  @BeforeEach
  void setup() {
    this.rsaKey = new RsaConfiguration().rsaKey();
    ReflectionTestUtils.setField(underTest, "rsaKey", rsaKey);
  }

  @Test
  @Disabled
  void givenValidUsernameAndPassword_whenLoginCalled_thenReturnSuccessfulSignInResponse()
      throws JOSEException, ParseException {
    //Given
    Account testAccount = Account.builder()
        .firstName("first-name")
        .lastName("last-name")
        .email("no@no.com")
        .username("username-1")
        .roles(Set.of(
            Role.builder()
                .name("role-1")
                .description("role 1 description")
                .permissions(Set.of(Permission.builder()
                    .name("permission.one")
                    .build()))
                .build()))
        .build();

    LoginRequestDto loginRequest = LoginRequestDto.builder()
        .username(testAccount.getUsername())
        .password("password")
        .build();

    Date expirationDate = new Date();
    String authorizationId = UUID.randomUUID().toString();

    Authentication mockAuthentication = mock(Authentication.class);

    ArgumentCaptor<Authorization> capturedAuthorization = ArgumentCaptor.forClass(
        Authorization.class);
    Authorization providedAuthorization = Authorization.builder()
        .accessToken(generateJwt(generateAccessTokenClaimsSet(testAccount, expirationDate)))
        .refreshToken(generateJwt(
            generateRefreshTokenClaimsSet(testAccount, authorizationId, expirationDate)))
        .idToken(generateJwt(generateIdTokenClaimsSet(testAccount, expirationDate)))
        .authorizationGuid(authorizationId)
        .account(testAccount)
        .build();

    when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication);
    when(mockAuthentication.isAuthenticated()).thenReturn(Boolean.TRUE);
    when(accountRepository.findByUsername(any())).thenReturn(Optional.of(testAccount));
    when(authorizationServerSettings.getIssuer()).thenReturn(ISSUER);
    when(authorizationService.saveAuthorization(any())).thenReturn(providedAuthorization);

    OAuth2LoginEndpoint underTestSpy = spy(underTest);

    //When
    ResponseEntity<SuccessfulLoginResponseDto> returnedLoginResponse = underTestSpy.login(
        loginRequest);

    //Then
    verify(authorizationService).saveAuthorization(capturedAuthorization.capture());

    Authorization capturedAuthorizationValue = capturedAuthorization.getValue();

    String responseAccessTokenString = Objects.requireNonNull(returnedLoginResponse.getBody())
        .getAccessToken();
    JWTClaimsSet responseAccessTokenClaimsSet = extractClaimsSet(responseAccessTokenString);
    String responseRefreshTokenString = Objects.requireNonNull(returnedLoginResponse.getBody())
        .getAccessToken();
    JWTClaimsSet responseRefreshTokenClaimsSet = extractClaimsSet(responseRefreshTokenString);
    String responseIdTokenString = Objects.requireNonNull(returnedLoginResponse.getBody())
        .getAccessToken();
    JWTClaimsSet responseIdTokenClaimsSet = extractClaimsSet(responseIdTokenString);

    String savedAccessTokenString = capturedAuthorizationValue.getAccessToken();
    JWTClaimsSet savedAccessTokenClaimsSet = extractClaimsSet(savedAccessTokenString);
    String savedRefreshTokenString = capturedAuthorizationValue.getAccessToken();
    JWTClaimsSet savedRefreshTokenClaimsSet = extractClaimsSet(savedRefreshTokenString);
    String savedIdTokenString = capturedAuthorizationValue.getAccessToken();
    JWTClaimsSet savedTokenClaimsSet = extractClaimsSet(savedIdTokenString);

    String expectedAccessTokenString = providedAuthorization.getAccessToken();
    JWTClaimsSet expectedAccessTokenClaimsSet = extractClaimsSet(expectedAccessTokenString);
    String expectedRefreshTokenString = providedAuthorization.getAccessToken();
    JWTClaimsSet expectedRefreshTokenClaimsSet = extractClaimsSet(expectedRefreshTokenString);
    String expectedIdTokenString = providedAuthorization.getAccessToken();
    JWTClaimsSet expectedTokenClaimsSet = extractClaimsSet(expectedIdTokenString);

    assertThat(responseAccessTokenClaimsSet.getClaims())
        .extracting("sub", "aud", "scope", "iss", "groups")
        .contains(
            expectedAccessTokenClaimsSet.getClaims().get("sub"),
            expectedAccessTokenClaimsSet.getClaims().get("aud"),
            expectedAccessTokenClaimsSet.getClaims().get("scope"),
            expectedAccessTokenClaimsSet.getClaims().get("iss"),
            expectedAccessTokenClaimsSet.getClaims().get("groups")
        );
    assertThat(savedAccessTokenClaimsSet.getClaims())
        .extracting("sub", "aud", "scope", "iss", "groups")
        .contains(
            expectedAccessTokenClaimsSet.getClaims().get("sub"),
            expectedAccessTokenClaimsSet.getClaims().get("aud"),
            expectedAccessTokenClaimsSet.getClaims().get("scope"),
            expectedAccessTokenClaimsSet.getClaims().get("iss"),
            expectedAccessTokenClaimsSet.getClaims().get("groups")
        );
  }


  private JWTClaimsSet generateAccessTokenClaimsSet(Account account, Date expirationTime) {
    return new JWTClaimsSet.Builder().issuer(ISSUER)
        .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername()))
        .claim("scope", account.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" ")))
        .claim("groups", account.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .expirationTime(expirationTime)
        .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now()))
        .jwtID(UUID.randomUUID().toString())
        .build();
  }

  private JWTClaimsSet generateRefreshTokenClaimsSet(Account account, String authorizationId,
      Date expirationTime) {
    return new JWTClaimsSet.Builder().issuer(ISSUER)
        .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername()))
        .claim("authorizationId", authorizationId)
        .expirationTime(expirationTime)
        .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now()))
        .jwtID(UUID.randomUUID().toString())
        .build();
  }

  private JWTClaimsSet generateIdTokenClaimsSet(Account account, Date expirationTime) {
    return new JWTClaimsSet.Builder().issuer(ISSUER)
        .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername()))
        .claim("idInfo", IdTokenInfoDto.MAPPER.accountToIdTokenInfoDto(account))
        .expirationTime(expirationTime)
        .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now()))
        .jwtID(UUID.randomUUID().toString())
        .build();
  }


  private String generateJwt(JWTClaimsSet claimsSet)
      throws JOSEException {
    SignedJWT signedJWT;

    JWSSigner signer = new RSASSASigner(this.rsaKey);
    signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
    signedJWT.sign(signer);

    return signedJWT.serialize();
  }

  private JWTClaimsSet extractClaimsSet(String token) throws ParseException {
    SignedJWT signedJWT = SignedJWT.parse(token);

    return signedJWT.getJWTClaimsSet();
  }

  private boolean isTokenProperlySigned(String token) throws ParseException, JOSEException {
    SignedJWT signedJWT = SignedJWT.parse(token);

    return signedJWT.verify(getRSASSAVerifier());
  }

  private RSASSAVerifier getRSASSAVerifier() throws JOSEException {
    return new RSASSAVerifier(this.rsaKey);
  }
}