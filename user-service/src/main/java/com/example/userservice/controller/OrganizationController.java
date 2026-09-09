package com.example.userservice.controller;

import com.example.userservice.dto.OrganizationRequest;
import com.example.userservice.entity.Organization;
import com.example.userservice.entity.User;
import com.example.userservice.security.CurrentUserProvider;
import com.example.userservice.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Organization management")
public class OrganizationController {

  private final OrganizationService organizationService;
  private final CurrentUserProvider currentUserProvider;

  public OrganizationController(
      OrganizationService organizationService, CurrentUserProvider currentUserProvider) {
    this.organizationService = organizationService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create an organization (SUPER_ADMIN only)")
  public ResponseEntity<Organization> create(@Valid @RequestBody OrganizationRequest request) {
    User caller = currentUserProvider.require();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(organizationService.create(caller, request));
  }

  @GetMapping
  @Operation(summary = "List organizations (SUPER_ADMIN: all; others: own organization only)")
  public ResponseEntity<Page<Organization>> list(@PageableDefault(size = 20) Pageable pageable) {
    User caller = currentUserProvider.require();
    return ResponseEntity.ok(organizationService.list(caller, pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get an organization by id (own organization only, unless SUPER_ADMIN)")
  public ResponseEntity<Organization> get(@PathVariable Long id) {
    User caller = currentUserProvider.require();
    return ResponseEntity.ok(organizationService.get(caller, id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an organization (SUPER_ADMIN only)")
  public ResponseEntity<Organization> update(
      @PathVariable Long id, @Valid @RequestBody OrganizationRequest request) {
    User caller = currentUserProvider.require();
    return ResponseEntity.ok(organizationService.update(caller, id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete an organization (SUPER_ADMIN only)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    User caller = currentUserProvider.require();
    organizationService.delete(caller, id);
    return ResponseEntity.noContent().build();
  }
}
