package com.cairnfg.waypoint.authorization.entity;

import java.time.LocalDateTime;

public interface IBaseEntity<T> {

  T getId();

  LocalDateTime getCreated();

  LocalDateTime getUpdated();

  String getModifiedBy();
}
