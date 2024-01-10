package com.cairnfg.waypoint.authorization.controller.login;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Permission;
import com.cairnfg.waypoint.authorization.repository.AccountRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class OAuth2LoginEndpoint {
    public static final String PATH = "/api/oauth/token";

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuthorizationServerSettings authorizationServerSettings;

    @Autowired
    private RSAKey rsaKey;

    @PreAuthorize("permitAll()")
    @PostMapping(PATH)
    public ResponseEntity<String> login() throws JOSEException {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken("test_username", "password");
        if (authenticationManager.authenticate(authenticationToken).isAuthenticated()) {
            Account account = accountRepository.findByUsername("test_username").get();
            return ResponseEntity.ok(generateJwt(generateAccessTokenClaimsSet(account)));
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public JWTClaimsSet generateAccessTokenClaimsSet(Account account) {
        return new JWTClaimsSet.Builder().issuer(this.authorizationServerSettings.getIssuer())
                .subject(account.getUsername()).audience(Collections.singletonList(account.getUsername()))
                .claim("scope", account.getPermissions().stream().map(Permission::getName).collect(Collectors.joining(" ")))
                .claim("groups", account.getPermissions().stream().map(Permission::getName).collect(Collectors.toList()))
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
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
