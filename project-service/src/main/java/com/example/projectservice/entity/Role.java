package com.example.projectservice.entity;

/**
 * Role hierarchy, from lowest to highest privilege level. Mirrors the canonical role set owned by
 * user-service.
 */
public enum Role {
  VIEWER(1),
  MEMBER(2),
  MANAGER(3),
  ORG_ADMIN(4),
  SUPER_ADMIN(5);

  private final int level;

  Role(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }
}
