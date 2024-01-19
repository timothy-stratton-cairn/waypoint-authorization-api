package com.cairnfg.waypoint.authorization.entity;

import com.cairnfg.waypoint.authorization.entity.converter.EncryptedFieldConverter;
import com.cairnfg.waypoint.authorization.entity.enumeration.AccountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@Entity
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class Account implements BaseEntity<Long>, UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CreationTimestamp
    private LocalDateTime created;
    @UpdateTimestamp
    private LocalDateTime updated;
    @Builder.Default
    private String modifiedBy = "system";
    private String username;
    private String password;
    @Convert(converter = EncryptedFieldConverter.class)
    private String firstName;
    @Convert(converter = EncryptedFieldConverter.class)
    private String lastName;
    @Convert(converter = EncryptedFieldConverter.class)
    private String email;
    @Convert(converter = EncryptedFieldConverter.class)
    private String address1;
    @Convert(converter = EncryptedFieldConverter.class)
    private String address2;
    private String city;
    private String state;
    @Convert(converter = EncryptedFieldConverter.class)
    private String zip;
    private Boolean accountExpired;
    private Boolean accountLocked;
    private Boolean credentialsExpired;
    private Boolean enabled;
    @Column(name = "accepted_tc")
    private Boolean acceptedTC; //terms and conditions
    @Column(name = "accepted_eula")
    private Boolean acceptedEULA; //end-user licensing agreement
    @Column(name = "accepted_pa")
    private Boolean acceptedPA; //privacy agreement

    @Enumerated(EnumType.ORDINAL)
    private AccountType accountType;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "account_permission",
            joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id"))
    private Collection<Permission> permissions;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.permissions;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
