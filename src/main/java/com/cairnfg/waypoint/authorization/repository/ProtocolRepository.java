package com.cairnfg.waypoint.authorization.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

@Repository
public class ProtocolRepository {

  private final WebClient webClient;

  public ProtocolRepository(@Qualifier("dashboardApiWebClient") WebClient webclient) {
    this.webClient = webclient;
  }

  public void addCoClientToAllProtocolsForAccount(Long accountId, Long coClientId) {
    final String PATH = "/api/protocol/account/{accountId}/{coClientId}";

    webClient.post()
        .uri(PATH, accountId, coClientId)
        .header("Authorization",
            "Bearer " + ((Jwt) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal()).getTokenValue())
        .retrieve()
        .bodyToMono(String.class)
        .block();
  }

}
