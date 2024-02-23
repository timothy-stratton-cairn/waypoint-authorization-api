package com.cairnfg.waypoint.authorization.service;


import com.cairnfg.waypoint.authorization.entity.Authorization;
import com.cairnfg.waypoint.authorization.repository.AuthorizationRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

  private final AuthorizationRepository authorizationRepository;

  public AuthorizationService(AuthorizationRepository authorizationRepository) {
    this.authorizationRepository = authorizationRepository;
  }

  public Authorization saveAuthorization(Authorization authorization) {
    return this.authorizationRepository.save(authorization);
  }
}
