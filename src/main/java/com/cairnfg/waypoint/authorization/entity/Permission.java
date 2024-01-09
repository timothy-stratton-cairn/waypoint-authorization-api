package com.cairnfg.waypoint.authorization.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permission")
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity implements GrantedAuthority {
    private String name;
    private String description;

    @Override
    public String getAuthority() {
        return this.name;
    }
}
