package com.example.userservice.controller;

import com.example.userservice.dto.RoleChangeRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.entity.User;
import com.example.userservice.security.CurrentUserProvider;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management within organizations")
public class UserController {

  private final UserService userService;
  private final CurrentUserProvider currentUserProvider;

  public UserController(UserService userService, CurrentUserProvider currentUserProvider) {
    this.userService = userService;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  @Operation(
      summary =
          "List users, scoped to the caller's organization (SUPER_ADMIN can filter by"
              + " organizationId)")
  public ResponseEntity<Page<UserResponse>> list(
      @RequestParam(name = "organizationId", required = false) Long organizationId,
      @PageableDefault(size = 20) Pageable pageable) {
    User caller = currentUserProvider.require();
    return ResponseEntity.ok(userService.list(caller, organizationId, pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a single user by id (same organization only, unless SUPER_ADMIN)")
  public ResponseEntity<UserResponse> get(@PathVariable Long id) {
    User caller = currentUserProvider.require();
    return ResponseEntity.ok(userService.get(caller, id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a user's profile (self, ORG_ADMIN of same org, or SUPER_ADMIN)")
  public ResponseEntity<UserResponse> update(
      @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
    User caller = currentUserProvider.require();
    return ResponseEntity.ok(userService.update(caller, id, request));
  }

  @PatchMapping("/{id}/role")
  @Operation(
      summary = "Change a user's role (ORG_ADMIN of same org capped at own level, or SUPER_ADMIN)")
  public ResponseEntity<UserResponse> changeRole(
      @PathVariable Long id, @Valid @RequestBody RoleChangeRequest request) {
    User caller = currentUserProvider.require();
    return ResponseEntity.ok(userService.changeRole(caller, id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Deactivate a user (ORG_ADMIN of same org, or SUPER_ADMIN)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    User caller = currentUserProvider.require();
    userService.deactivate(caller, id);
    return ResponseEntity.noContent().build();
  }
}
