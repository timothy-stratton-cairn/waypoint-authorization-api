package com.cairnfg.waypoint.authorization.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinksDto {

  private String resetPasswordLink;
}
