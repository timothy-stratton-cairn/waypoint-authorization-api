package com.cairnfg.waypoint.authorization.entity;

import com.cairnfg.waypoint.authorization.entity.converter.EncryptedFieldConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
  private Boolean accountLocked;

  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime accountExpirationDate;

  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime passwordExpirationDate;
  private Boolean enabled;
  @Column(name = "accepted_tc")
  private Boolean acceptedTC; //terms and conditions
  @Column(name = "accepted_eula")
  private Boolean acceptedEULA; //end-user licensing agreement
  @Column(name = "accepted_pa")
  private Boolean acceptedPA; //privacy agreement

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinTable(name = "account_role",
      joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
  private Collection<Role> roles;

  @Override
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.roles.stream()
        .map(Role::getPermissions)
        .flatMap(Collection::stream)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean isAccountNonExpired() {
    return this.accountExpirationDate.isAfter(LocalDateTime.now());
  }

  @Override
  public boolean isAccountNonLocked() {
    return !this.accountLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return this.passwordExpirationDate.isAfter(LocalDateTime.now());
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }
}
