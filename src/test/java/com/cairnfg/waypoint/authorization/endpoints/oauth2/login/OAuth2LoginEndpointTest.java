package com.cairnfg.waypoint.authorization.endpoints.oauth2.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
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
  private final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1QM2gqri6Kca+l3dLmccwhHL5/0MxMNqn6AT02wM5g4WnVtMLtv6VF9jl9O6sPp2WcVqo4Ag9DyFK+cFmuL1pBW2LZlfAU1/EakhElgFy2qdhWAODCePBp3Oypa98wTIzIwSXPO9CIe2v6XpV+cIAiGCvmy83jz25vy2ZmWE3+jg8PQHeUAHe9f6kJrTlI/yfSXfSM+VxL714Hmaarf58+KMAldAnT8iMTy+bSZXV48DMIeKntLa+kDq/QzjMM7Evx5aU3SmRubND7dIRnxDmacEKfWO5F6K7+V0m1Ku52PgBczFvS2B06d7fdkZrVmQYzLlMmQ7zrk8hT+vNxySjwIDAQAB";
  private final String privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDVAzaCquLopxr6Xd0uZxzCEcvn/QzEw2qfoBPTbAzmDhadW0wu2/pUX2OX07qw+nZZxWqjgCD0PIUr5wWa4vWkFbYtmV8BTX8RqSESWAXLap2FYA4MJ48Gnc7Klr3zBMjMjBJc870Ih7a/pelX5wgCIYK+bLzePPbm/LZmZYTf6ODw9Ad5QAd71/qQmtOUj/J9Jd9Iz5XEvvXgeZpqt/nz4owCV0CdPyIxPL5tJldXjwMwh4qe0tr6QOr9DOMwzsS/HlpTdKZG5s0Pt0hGfEOZpwQp9Y7kXorv5XSbUq7nY+AFzMW9LYHTp3t92RmtWZBjMuUyZDvOuTyFP683HJKPAgMBAAECggEAXHhSl5LbgifYvEs3E0fL4iubmw/tFRXIGZeCQ8uqJcyD4LebcNQp7aqjL2vIpb901S+wRV3/8Ea9gPt/IWfZPuv19AGajpQ3z1DefHodudqEmzvBnWEm47Zp6ORbfT3cdX9xEUz2CNfxa2z4ySIQBezKNyQ5MTjB7jBSdk0ny5MfWsHUKmzJgcnfYiuu1k28NWqi+CZbv4DwGXSGxViKpdayzxaIgPIb5adrq/KsmXBsARVfXXscZPbr7Ik3cd1ENZ25wREW+1NZVH3O71Nv1iL7XmDYxD8s7qNaX7h00rVUTjJ6yXbQFh9nuuRcwLJ4C8589jgPHott9LkTSTy28QKBgQD/ATquLbN+/ybJ1pIDZaAUMGElfX82bJQVNdMwP+ubPoTgpDTRaRtaaPF9ymb+FMdNFVR3xdf3TXFC2qSyJSa21s1vBOvyjMOBI5ZxpYRBwMbvKI1Ju2tSRPivEHcZgxBSdb28y1grTseoQtWc4x4dnjuIouXNgTYExsgBFJEbTQKBgQDV2Aet4FXXqptVAdlLHhGyd9ORw0/C8C15ANj7mRGoDwe/lGZbVxn2+KgaOAsOEg3wSxgAvEV3yRJhNxMfU8esaZ8VZLdQAT0wB32djT3E5ZwGF5hUFlMBi85wP1ZHWUHV10OFlr0eZcftVzs1ML9IrFMCwG9kSpL/BIlaSVhfSwKBgGIljvhLDrLqcXtt4nT8QEiE9l7/ll7UG4quVMJVV9FAltP/X3TaB3UcvxWzOUDBrpO0ifYvZ5f/boBUtt75/ZSRyC/arRWFqBmkINtM4UKNeAPEj9y5De10LmLy62QeVcHnH0fy29SPVbklQCJKJ+UuaauFRv15pdjgyUnhKJhhAoGAfpHdCbdMV9gtFEWB3Yowo1d1LENUBGewInyz8UcKQlSTG3u0kb+eKKXIqaweLvfUtDNeQ6Er5NhoPo4gjVxDRPNHK5xvO7VgB/uHCCe/NblJffmdObf07WdYiuuSH6ze4LkGyB0OVt2bVglZjAQMU/FbOLe5trzFAJgoy/25hr0CgYBXQu3YDtcmpQdFOA91IpAkP8DlZwF3MwCmqnhiYCBwDX/gmWc5M603Wrc664K11cw6i7MES4gemFDeRdVDmq2aqRKSGm/GamWF7XYgL90YY7xlmuJSQZWCjOGUsxEFGkY2T2o524Bv7bsjmMNLbjRl8OLm6njyVHgMxrMaj/f7lQ==";
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

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(underTest, "privateKey", privateKey);
    ReflectionTestUtils.setField(underTest, "publicKey", publicKey);
  }

  @Test
  @Disabled
  void givenValidUsernameAndPassword_whenLoginCalled_thenReturnSuccessfulSignInResponse()
      throws JOSEException, ParseException, InvalidKeySpecException, NoSuchAlgorithmException {
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
      throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {
    SignedJWT signedJWT;

    JWSSigner signer = new RSASSASigner(rsaKey());
    signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
    signedJWT.sign(signer);

    return signedJWT.serialize();
  }

  private JWTClaimsSet extractClaimsSet(String token) throws ParseException {
    SignedJWT signedJWT = SignedJWT.parse(token);

    return signedJWT.getJWTClaimsSet();
  }

  private boolean isTokenProperlySigned(String token)
      throws ParseException, JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {
    SignedJWT signedJWT = SignedJWT.parse(token);

    return signedJWT.verify(getRSASSAVerifier());
  }

  private RSASSAVerifier getRSASSAVerifier()
      throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {
    return new RSASSAVerifier(rsaKey());
  }

  private PublicKey getPublicKey()
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey.getBytes(StandardCharsets.UTF_8));
    X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(spec);
  }

  private PrivateKey getPrivateKey()
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    byte[] privateKeyBytes = Base64.getDecoder()
        .decode(privateKey.getBytes(StandardCharsets.UTF_8));
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    KeyFactory fact = KeyFactory.getInstance("RSA");
    return fact.generatePrivate(keySpec);
  }

  public RSAKey rsaKey()
      throws InvalidKeySpecException, NoSuchAlgorithmException {
    RSAPublicKey publicKey = (RSAPublicKey) getPublicKey();
    RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey();
    return new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(UUID.randomUUID().toString())
        .build();
  }
}