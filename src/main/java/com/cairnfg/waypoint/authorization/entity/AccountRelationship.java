package com.cairnfg.waypoint.authorization.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("active=1")
@Table(name = "account_relationship")
public class AccountRelationship extends BaseEntity implements IBaseEntity<Long> {

  @JoinColumn(name = "main_account_id", nullable = false)
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private Account mainAccount;

  @JoinColumn(name = "co_client_id")
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private Account coClient;

  @JoinColumn(name = "dependent_id")
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  private Account dependent;
}
