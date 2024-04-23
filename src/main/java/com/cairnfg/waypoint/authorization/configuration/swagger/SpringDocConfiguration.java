package com.cairnfg.waypoint.authorization.configuration.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {

  @Value("${waypoint.authorization.base-url}")
  private String baseUrl;

  @Bean
  public OpenAPI waypointAuthorizationApiOpenApi() {
    return new OpenAPI()
        .addServersItem(new Server().description("This server").url(baseUrl))
        .addServersItem(new Server().description("Dev Server").url("http://96.61.158.12:8082"))
        .addServersItem(new Server().description("Local Server").url("http://localhost:8082"))
        .info(new Info()
            .title("Waypoint - Authorization API")
            .description(
                "Waypoint - Authorization API provides the login, authorization, authentication, and user related features for Waypoint")
            .version("LATEST"))
        .components(
            new Components().addSecuritySchemes("oAuth2JwtBearer",
                new SecurityScheme().name("oAuth2JwtBearer")
                    .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
  }
}
