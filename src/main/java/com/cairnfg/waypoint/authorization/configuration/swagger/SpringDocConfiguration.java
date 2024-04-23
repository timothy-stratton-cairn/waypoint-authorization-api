package com.cairnfg.waypoint.authorization.configuration.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfiguration {

  @Bean
  public OpenAPI waypointAuthorizationApiOpenApi() {
    return new OpenAPI()
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
