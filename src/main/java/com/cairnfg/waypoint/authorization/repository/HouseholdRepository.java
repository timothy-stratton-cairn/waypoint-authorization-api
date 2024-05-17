package com.cairnfg.waypoint.authorization.repository;

import com.cairnfg.waypoint.authorization.entity.Household;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdRepository extends JpaRepository<Household, Long> {

  Optional<Household> findByHouseholdAccounts_Id(Long householdAccountId);
}
