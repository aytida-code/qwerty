package com.example.userservice.dto;

import com.example.userservice.entity.Role;
import jakarta.validation.constraints.NotNull;

public class RoleChangeRequest {

  @NotNull private Role role;

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }
}
