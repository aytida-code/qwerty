package com.example.projectservice.dto;

import com.example.projectservice.entity.ProjectMember;
import java.time.Instant;

public class MemberResponse {

  private Long id;
  private Long projectId;
  private Long userId;
  private Instant createdAt;

  public static MemberResponse from(ProjectMember member) {
    MemberResponse dto = new MemberResponse();
    dto.id = member.getId();
    dto.projectId = member.getProjectId();
    dto.userId = member.getUserId();
    dto.createdAt = member.getCreatedAt();
    return dto;
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public Long getUserId() {
    return userId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
