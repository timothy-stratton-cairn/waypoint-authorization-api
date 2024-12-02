package com.cairnfg.waypoint.authorization.repository;

import com.cairnfg.waypoint.authorization.entity.AccountRelationship;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRelationshipRepository extends JpaRepository<AccountRelationship, Long> {

  List<AccountRelationship> findByDependentIdIn(List<Long> dependentIds);

  Optional<AccountRelationship> findByMainAccountId(Long mainAccountId);

  List<AccountRelationship> findByCoClientId(Long coClientId);

  List<AccountRelationship> findByDependentId(Long dependentId);

  List<AccountRelationship> findByActiveTrue();
}
