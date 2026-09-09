package com.example.userservice.dto;

import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import java.time.Instant;

public class UserResponse {

  private Long id;
  private String name;
  private String email;
  private Long organizationId;
  private Role role;
  private boolean active;
  private Instant createdAt;

  public static UserResponse from(User user) {
    UserResponse dto = new UserResponse();
    dto.id = user.getId();
    dto.name = user.getName();
    dto.email = user.getEmail();
    dto.organizationId = user.getOrganizationId();
    dto.role = user.getRole();
    dto.active = user.isActive();
    dto.createdAt = user.getCreatedAt();
    return dto;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  public Role getRole() {
    return role;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
