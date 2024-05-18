package com.cairnfg.waypoint.authorization.service;

import com.cairnfg.waypoint.authorization.entity.Household;
import com.cairnfg.waypoint.authorization.repository.HouseholdRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class HouseholdService {

  private final HouseholdRepository householdRepository;

  public HouseholdService(HouseholdRepository householdRepository) {
    this.householdRepository = householdRepository;
  }

  public List<Household> getAllHouseholds() {
    return this.householdRepository.findAll();
  }

  public Optional<Household> getHouseholdForAccount(Long accountId) {
    return this.householdRepository.findByHouseholdAccounts_Id(accountId);
  }

  public Household saveHousehold(Household household) {
    return this.householdRepository.save(household);
  }

  public Optional<Household> getHouseholdById(Long id) {
    return this.householdRepository.findById(id);
  }

  public Optional<Household> getHouseholdByName(String name) {
    return this.householdRepository.findByName(name);
  }
}
