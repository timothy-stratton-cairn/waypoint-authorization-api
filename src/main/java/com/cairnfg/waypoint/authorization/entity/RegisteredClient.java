package com.cairnfg.waypoint.authorization.entity;

import com.cairnfg.waypoint.authorization.entity.converter.AbstractObjectConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Data
@Entity
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "oauth2_registered_client")
public class RegisteredClient extends
    org.springframework.security.oauth2.server.authorization.client.RegisteredClient implements
    IBaseEntity<String>, Serializable {

  @Serial
  private static final long serialVersionUID = 7526472295622776147L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "registered_client_id", unique = true, nullable = false)
  private Long registeredClientId;
  @lombok.Builder.Default
  private String id = UUID.randomUUID().toString();
  @CreationTimestamp
  private LocalDateTime created;
  @UpdateTimestamp
  private LocalDateTime updated;
  @lombok.Builder.Default
  private String modifiedBy = "system";

  @lombok.Builder.Default
  private Boolean active = Boolean.TRUE;

  private String clientId;
  private Instant clientIdIssuedAt;
  private String clientSecret;
  private Instant clientSecretExpiresAt;
  private String clientName;

  @Column(name = "client_authentication_methods", length = 1000, columnDefinition = "TEXT")
  @Convert(converter = AbstractObjectConverter.class)
  private Set<ClientAuthenticationMethod> clientAuthenticationMethods;

  @Column(name = "authorization_grant_types", length = 1000, columnDefinition = "TEXT")
  @Convert(converter = AbstractObjectConverter.class)
  private Set<AuthorizationGrantType> authorizationGrantTypes;

  @Column(name = "redirect_uris", length = 1000, columnDefinition = "TEXT")
  @Convert(converter = AbstractObjectConverter.class)
  private Set<String> redirectUris;

  @Column(name = "post_logout_redirect_uris", length = 1000, columnDefinition = "TEXT")
  @Convert(converter = AbstractObjectConverter.class)
  private Set<String> postLogoutRedirectUris;

  @Column(name = "scopes", length = 1000, columnDefinition = "TEXT")
  @Convert(converter = AbstractObjectConverter.class)
  private Set<String> scopes;


  @Column(name = "client_settings", length = 1000, columnDefinition = "TEXT")
  @Convert(converter = AbstractObjectConverter.class)
  private ClientSettings clientSettings;


  @Column(name = "token_settings", length = 1000, columnDefinition = "TEXT")
  @Convert(converter = AbstractObjectConverter.class)
  private TokenSettings tokenSettings;
}
