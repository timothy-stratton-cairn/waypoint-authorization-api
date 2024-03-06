package com.cairnfg.waypoint.authorization.endpoints.oauth2;

import com.cairnfg.waypoint.authorization.endpoints.oauth2.dto.LoginRequestDto;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.dto.SuccessfulLoginResponseDto;
import com.cairnfg.waypoint.authorization.endpoints.oauth2.service.OAuth2Service;
import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Authorization;
import com.cairnfg.waypoint.authorization.service.AccountService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "OAuth2")
public class OAuth2LoginEndpoint {

  public static final String PATH = "/api/oauth/token";

  private final AuthenticationManager authenticationManager;
  private final AccountService accountService;
  private final OAuth2Service oAuth2Service;

  public OAuth2LoginEndpoint(AuthenticationManager authenticationManager,
      AccountService accountService,
      OAuth2Service oAuth2Service) {
    this.authenticationManager = authenticationManager;
    this.accountService = accountService;
    this.oAuth2Service = oAuth2Service;
  }

  @PostMapping(PATH)
  @PreAuthorize("permitAll()")
  @Operation(summary = "Signs in using OAuth2.",
      description =
          "Signs in a user or client, and issues a JWT containing access permissions and other metadata."
              +
              " Endpoint can also be used to refresh an authorization without having to login again.",
      responses = {
          @ApiResponse(responseCode = "200",
              content = {@Content(mediaType = "application/json",
                  schema = @Schema(implementation = SuccessfulLoginResponseDto.class))}),
          @ApiResponse(responseCode = "401", description = "Unauthorized",
              content = {@Content(schema = @Schema(hidden = true))})})
  public ResponseEntity<SuccessfulLoginResponseDto> login(@RequestBody LoginRequestDto loginRequest)
      throws JOSEException {
    log.info("Attempting to login to user account with username [{}]", loginRequest.getUsername());
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
            loginRequest.getPassword());

    if (this.authenticationManager.authenticate(authenticationToken).isAuthenticated()) {
      Account account = this.accountService.findByUsername(loginRequest.getUsername())
          .orElseThrow(EntityNotFoundException::new);
      Date expiresAt = Date.from(Instant.now().plusSeconds(3600));

      Authorization authorization = oAuth2Service.generateOAuth2TokenFamily(account, expiresAt);

      return generateSuccessResponse(authorization, expiresAt);
    } else {
      log.info("Login attempt to user account with username [{}] failed",
          loginRequest.getUsername());
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }

  private ResponseEntity<SuccessfulLoginResponseDto> generateSuccessResponse(
      Authorization authorization, Date expiresAt) {
    SuccessfulLoginResponseDto responseDto = SuccessfulLoginResponseDto.builder()
        .accessToken(authorization.getAccessToken())
        .refreshToken(authorization.getRefreshToken())
        .idToken(authorization.getIdToken())
        .expiresIn(expiresAt.getTime() - new Date().getTime())
        .permissions(authorization.getAccount()
            .getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList())
        .build();

    return ResponseEntity.ok(responseDto);
  }
}
