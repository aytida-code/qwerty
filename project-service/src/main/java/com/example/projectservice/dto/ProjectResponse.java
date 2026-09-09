package com.example.projectservice.dto;

import com.example.projectservice.entity.Project;
import java.time.Instant;

public class ProjectResponse {

  private Long id;
  private String name;
  private String description;
  private Long organizationId;
  private Long createdBy;
  private String status;
  private Instant createdAt;

  public static ProjectResponse from(Project project) {
    ProjectResponse dto = new ProjectResponse();
    dto.id = project.getId();
    dto.name = project.getName();
    dto.description = project.getDescription();
    dto.organizationId = project.getOrganizationId();
    dto.createdBy = project.getCreatedBy();
    dto.status = project.getStatus();
    dto.createdAt = project.getCreatedAt();
    return dto;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
