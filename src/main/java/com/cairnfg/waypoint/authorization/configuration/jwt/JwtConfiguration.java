package com.cairnfg.waypoint.authorization.configuration.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

@Configuration
public class JwtConfiguration {

  @Bean
  public BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter(
      JwtAuthenticationProvider jwtAuthProvider) {
    AuthenticationManager jwtAuthMgr = new ProviderManager(jwtAuthProvider);
    return new BearerTokenAuthenticationFilter(jwtAuthMgr);
  }
}
