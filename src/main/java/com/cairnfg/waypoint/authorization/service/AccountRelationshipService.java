package com.cairnfg.waypoint.authorization.service;

import com.cairnfg.waypoint.authorization.entity.AccountRelationship;
import com.cairnfg.waypoint.authorization.repository.AccountRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountRelationshipService {

  private final AccountRelationshipRepository repository;

  @Autowired
  public AccountRelationshipService(AccountRelationshipRepository repository) {
    this.repository = repository;
  }

  public Optional<AccountRelationship> findByMainAccountId(Long mainAccountId) {
    return repository.findByMainAccountId(mainAccountId);
  }

  public List<AccountRelationship> findByCoClientId(Long coClientId) {
    return repository.findByCoClientId(coClientId);
  }

  public List<AccountRelationship> findByDependentId(Long dependentId) {
    return repository.findByDependentId(dependentId);
  }

  public List<AccountRelationship> findByDependentIdIn(List<Long> dependentIds) {
    return repository.findByDependentIdIn(dependentIds);
  }

  public List<AccountRelationship> findActiveRelationships() {
    return repository.findByActiveTrue();
  }

  public AccountRelationship save(AccountRelationship accountRelationship) {
    return repository.save(accountRelationship);
  }

  public void deleteById(Long id) {
    repository.deleteById(id);
  }

  public List<AccountRelationship> findAll() {
    return repository.findAll();
  }
}
