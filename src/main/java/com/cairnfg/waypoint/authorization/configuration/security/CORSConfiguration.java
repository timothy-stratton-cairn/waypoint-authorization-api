package com.cairnfg.waypoint.authorization.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CORSConfiguration {

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            // TODO update allowedOrigins with the frontend URL when it's stood up
            .allowedOrigins("*")
            /*
             * Only GET, HEAD, and POST methods are allowed by default, so in order to allow other
             * Request methods either manually add them or allow all with (*)
             */
            .allowedMethods("*");
      }
    };
  }
}
