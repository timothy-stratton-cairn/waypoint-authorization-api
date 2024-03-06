package com.cairnfg.waypoint.authorization.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;

@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("active=1")
@Table(name = "permission")
public class Permission extends BaseEntity implements IBaseEntity<Long>, GrantedAuthority {

  private String name;
  private String description;

  @Override
  public String getAuthority() {
    return this.name;
  }
}
