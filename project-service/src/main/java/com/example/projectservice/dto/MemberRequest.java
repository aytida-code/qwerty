package com.example.projectservice.dto;

import jakarta.validation.constraints.NotNull;

public class MemberRequest {

  @NotNull private Long userId;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
