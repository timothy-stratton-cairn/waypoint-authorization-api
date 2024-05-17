package com.cairnfg.waypoint.authorization.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
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

  @JoinColumn(name = "primary_contact_account_id", nullable = false)
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private Account primaryContact;

  @OneToMany(mappedBy = "household", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private Set<Account> householdAccounts;
}
