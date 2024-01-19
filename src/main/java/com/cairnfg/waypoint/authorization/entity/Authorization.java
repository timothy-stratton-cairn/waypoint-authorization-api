package com.cairnfg.waypoint.authorization.entity;

import com.cairnfg.waypoint.authorization.entity.converter.EncryptedFieldConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "authorization")
public class Authorization implements BaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreationTimestamp
    private LocalDateTime created;
    @UpdateTimestamp
    private LocalDateTime updated;
    @Builder.Default
    private String modifiedBy = "system";

    private String authorizationGuid;
    @Column(length = 65535)
    @Convert(converter = EncryptedFieldConverter.class)
    private String accessToken;
    @Column(length = 65535)
    @Convert(converter = EncryptedFieldConverter.class)
    private String refreshToken;
    @Column(length = 65535)
    @Convert(converter = EncryptedFieldConverter.class)
    private String idToken;
    @JoinColumn
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Account account;
    @JoinColumn(name = "registered_client_id")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private RegisteredClient registeredClient;
}
