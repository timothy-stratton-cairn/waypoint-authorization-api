package com.cairnfg.waypoint.authorization.repository;

import com.cairnfg.waypoint.authorization.entity.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

  Optional<Account> findById(Long id);

  Optional<Account> findByUsername(String username);

  Optional<Account> findByEmail(String email);

  List<Account> findAllByHouseholdId(Long householdId);
}
