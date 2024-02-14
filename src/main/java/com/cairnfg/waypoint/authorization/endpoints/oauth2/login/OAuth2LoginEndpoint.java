package com.cairnfg.waypoint.authorization.endpoints.oauth2.login;

import com.cairnfg.waypoint.authorization.endpoints.login.dto.IdTokenInfoDto;
import com.cairnfg.waypoint.authorization.endpoints.login.dto.LoginRequestDto;
import com.cairnfg.waypoint.authorization.endpoints.login.dto.SuccessfulLoginResponseDto;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Authorization;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import com.cairnfg.waypoint.authorization.repository.AuthorizationRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private AuthorizationServerSettings authorizationServerSettings;

    @Autowired
    private RSAKey rsaKey;

    @PreAuthorize("permitAll()")
    @PostMapping(PATH)
    public ResponseEntity<SuccessfulLoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) throws JOSEException {
        log.info("Attempting to login to user account with username [{}]", loginRequest.getUsername());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        if (authenticationManager.authenticate(authenticationToken).isAuthenticated()) {
            Account account = accountRepository.findByUsername(loginRequest.getUsername()).get();
            Date expiresAt = Date.from(Instant.now().plusSeconds(3600));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            String authorizationId = UUID.randomUUID().toString();
            String accessToken = generateJwt(generateAccessTokenClaimsSet(account, expiresAt));
            String refreshToken = generateJwt(generateRefreshTokenClaimsSet(account, authorizationId, expiresAt));
            String idToken = generateJwt(generateIdTokenClaimsSet(account, expiresAt));

            Authorization authorization = Authorization.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .idToken(idToken)
                    .authorizationGuid(authorizationId)
                    .account(account)
                    .build();

            authorizationRepository.save(authorization);

            SuccessfulLoginResponseDto responseDto = SuccessfulLoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .idToken(idToken)
                    .expiresAt(formatter.format(expiresAt))
                    .permissions(account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .build();

            return ResponseEntity.ok(responseDto);
        }

        log.info("Login attempt to user account with username [{}] failed", loginRequest.getUsername());
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    public JWTClaimsSet generateAccessTokenClaimsSet(Account account, Date expirationTime) {
        return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
                .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername()))
                .claim("scope", account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" ")))
                .claim("groups", account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .expirationTime(expirationTime)
                .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now())).jwtID(UUID.randomUUID().toString())
                .build();
    }

    public JWTClaimsSet generateRefreshTokenClaimsSet(Account account, String authorizationId, Date expirationTime) {
        return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
                .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername()))
                .claim("authorizationId", authorizationId)
                .expirationTime(expirationTime)
                .notBeforeTime(Date.from(Instant.now())).issueTime(Date.from(Instant.now())).jwtID(UUID.randomUUID().toString())
                .build();
    }

    public JWTClaimsSet generateIdTokenClaimsSet(Account account, Date expirationTime) {
        return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
                .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername()))
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
