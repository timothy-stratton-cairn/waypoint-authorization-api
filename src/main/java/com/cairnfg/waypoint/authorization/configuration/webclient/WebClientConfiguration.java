package com.cairnfg.waypoint.authorization.configuration.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

  @Value("${waypoint.authorization.dashboard-api.base-url}")
  private String dashboardServerUri;

  @Bean(name = "dashboardApiWebClient")
  public WebClient dashboardApiWebClient() {
    return WebClient.create(dashboardServerUri);
  }
}
