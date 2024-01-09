package com.cairnfg.waypoint.authorization.entity;

import com.cairnfg.waypoint.authorization.entity.converter.EncryptedFieldConverter;
import com.cairnfg.waypoint.authorization.entity.enumeration.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity implements UserDetails {
    private String username;
    private String password;
    @Convert(converter = EncryptedFieldConverter.class)
    private String firstName;
    @Convert(converter = EncryptedFieldConverter.class)
    private String lastName;
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

    @Enumerated(EnumType.ORDINAL)
    private AccountType accountType;

    private List<Permission> permissions;

    @Override
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
