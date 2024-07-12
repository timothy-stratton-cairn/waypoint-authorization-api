package com.cairnfg.waypoint.authorization.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = "householdAccounts")
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("active=1")
@Table(name = "household")
public class Household extends BaseEntity implements IBaseEntity<Long> {

  private String name;
  private String description;

  @Transient
  private Set<Account> primaryContacts;

  @OneToMany(mappedBy = "household", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private Set<Account> householdAccounts;

  public Set<Account> getPrimaryContacts() {
    return householdAccounts.stream()
        .filter(account -> Objects.nonNull(account.getIsPrimaryContactForHousehold())
            && account.getIsPrimaryContactForHousehold())
        .collect(Collectors.toSet());
  }
}
