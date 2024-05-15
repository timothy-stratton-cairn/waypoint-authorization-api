package com.cairnfg.waypoint.authorization.service;

import com.cairnfg.waypoint.authorization.repository.ProtocolRepository;
import org.springframework.stereotype.Service;

@Service
public class ProtocolService {

  private final ProtocolRepository protocolRepository;

  public ProtocolService(final ProtocolRepository protocolRepository) {
    this.protocolRepository = protocolRepository;
  }

  public void addCoClientToAllProtocolsForAccount(Long accountId, Long coClientId) {
    this.protocolRepository.addCoClientToAllProtocolsForAccount(accountId,
        coClientId);
  }
}
