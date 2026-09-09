package com.example.taskservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read-only projection onto the "users" table owned by user-service, used exclusively by this
 * service's own ApiKeyFilter for authentication. This service does NOT own user data and never
 * writes to this table.
 */
@Entity
@Table(name = "users")
public class AuthUser {

  @Id private Long id;

  @Column(name = "organization_id")
  private Long organizationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "key_hash", nullable = false)
  private String keyHash;

  @Column(nullable = false)
  private boolean active;

  public Long getId() {
    return id;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  public Role getRole() {
    return role;
  }

  public String getKeyHash() {
    return keyHash;
  }

  public boolean isActive() {
    return active;
  }
}
