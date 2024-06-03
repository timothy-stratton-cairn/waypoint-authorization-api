package com.cairnfg.waypoint.authorization.service;

import com.cairnfg.waypoint.authorization.entity.Account;
import com.cairnfg.waypoint.authorization.entity.Household;
import com.cairnfg.waypoint.authorization.repository.HouseholdRepository;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class HouseholdService {

  private final HouseholdRepository householdRepository;

  public HouseholdService(HouseholdRepository householdRepository) {
    this.householdRepository = householdRepository;
  }

  public List<Household> getAllHouseholds() {
    return this.householdRepository.findAll().stream()
        .map(this::getPrimaryContactsForHousehold).collect(Collectors.toList());
  }

  public List<Household> getHouseholdListsByIdList(List<Long> ids) {
    return this.householdRepository.findAllById(ids).stream().distinct()
        .collect(Collectors.toList());
  }

  public Household saveHousehold(Household household) {
    return getPrimaryContactsForHousehold(this.householdRepository.save(household));
  }

  public Optional<Household> getHouseholdById(Long id) {
    return getPrimaryContactsForHousehold(this.householdRepository.findById(id));
  }

  public Optional<Household> getHouseholdByName(String name) {
    return getPrimaryContactsForHousehold(this.householdRepository.findByName(name));
  }

  public Optional<Household> getHouseholdByNameWithoutPrimaryContactPopulation(String name) {
    return this.householdRepository.findByName(name);
  }

  private Household getPrimaryContactsForHousehold(Household household) {
    try {
      household.setPrimaryContacts(household.getHouseholdAccounts().stream().filter(
          Account::getIsPrimaryContactForHousehold).collect(Collectors.toSet()));
    } catch (NullPointerException e) {
      household.setPrimaryContacts(Set.of(
          household.getHouseholdAccounts().stream()
              .filter(account -> account.getCoClient() != null || account.getDependents().isEmpty())
              .min(Comparator.comparing(Account::getCreated))
              .orElseThrow()));
    }
    return household;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private Optional<Household> getPrimaryContactsForHousehold(
      Optional<Household> householdOptional) {
    try {
      householdOptional.get()
          .setPrimaryContacts(householdOptional.get().getHouseholdAccounts().stream()
              .filter(Account::getIsPrimaryContactForHousehold).collect(Collectors.toSet()));
    } catch (NullPointerException e) {
      householdOptional.get().setPrimaryContacts(Set.of(
          householdOptional.get().getHouseholdAccounts().stream()
              .filter(account -> account.getCoClient() != null || account.getDependents().isEmpty())
              .min(Comparator.comparing(Account::getCreated))
              .orElseThrow()));
    } catch (NoSuchElementException e) {
      //Nothing to be done
    }
    return householdOptional;
  }
}
