package com.cairnfg.waypoint.authorization.entity;

import com.cairnfg.waypoint.authorization.entity.converter.EncryptedFieldConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("active=1")
@Table(name = "account")
public class Account extends BaseEntity implements IBaseEntity<Long>, UserDetails {

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

  @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinTable(name = "account_relationship",
      joinColumns = @JoinColumn(name = "main_account_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "co_client_id", referencedColumnName = "id"))
  private Account coClient;

  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinTable(name = "account_relationship",
      joinColumns = @JoinColumn(name = "main_account_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "dependent_id", referencedColumnName = "id"))
  private Set<Account> dependents;

  private Boolean accountLocked;

  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime accountExpirationDate;

  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime passwordExpirationDate;

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
    return this.getActive();
  }

  public Set<Account> getDependents() {
    if (dependents == null) {
      dependents = new LinkedHashSet<>();
    }
    return dependents;
  }
}
