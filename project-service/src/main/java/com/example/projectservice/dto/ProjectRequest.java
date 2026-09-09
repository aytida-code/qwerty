package com.example.projectservice.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectRequest {

  @NotBlank private String name;

  private String description;

  /** Only honoured for SUPER_ADMIN callers; others are scoped to their own organization. */
  private Long organizationId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(Long organizationId) {
    this.organizationId = organizationId;
  }
}
