package com.cairnfg.waypoint.authorization.entity;

import com.cairnfg.waypoint.authorization.entity.converter.AbstractObjectConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "registered_client")
@EqualsAndHashCode(callSuper = true)
public class RegisteredClient extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 7526472295622776147L;
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
