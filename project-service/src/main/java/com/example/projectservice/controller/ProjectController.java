package com.example.projectservice.controller;

import com.example.projectservice.dto.MemberRequest;
import com.example.projectservice.dto.MemberResponse;
import com.example.projectservice.dto.ProjectRequest;
import com.example.projectservice.dto.ProjectResponse;
import com.example.projectservice.entity.AuthUser;
import com.example.projectservice.security.ApiKeyFilter;
import com.example.projectservice.security.CurrentUserProvider;
import com.example.projectservice.service.ProjectService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Project management and member assignment")
public class ProjectController {

  private final ProjectService projectService;
  private final CurrentUserProvider currentUserProvider;

  public ProjectController(ProjectService projectService, CurrentUserProvider currentUserProvider) {
    this.projectService = projectService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create a project (MANAGER, ORG_ADMIN or SUPER_ADMIN)")
  public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(caller, request));
  }

  @GetMapping
  @Operation(summary = "List projects (org-scoped; MEMBER/VIEWER only see projects they belong to)")
  public ResponseEntity<Page<ProjectResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.ok(projectService.list(caller, pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a project by id (org-scoped + project membership for MEMBER/VIEWER)")
  public ResponseEntity<ProjectResponse> get(@PathVariable Long id) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.ok(projectService.get(caller, id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a project (MANAGER, ORG_ADMIN or SUPER_ADMIN of same organization)")
  public ResponseEntity<ProjectResponse> update(
      @PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.ok(projectService.update(caller, id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a project (MANAGER, ORG_ADMIN or SUPER_ADMIN of same organization)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    AuthUser caller = currentUserProvider.require();
    projectService.delete(caller, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/members")
  @Operation(summary = "Assign a member to a project (MANAGER, ORG_ADMIN or SUPER_ADMIN)")
  public ResponseEntity<MemberResponse> addMember(
      @PathVariable Long id,
      @Valid @RequestBody MemberRequest request,
      @RequestHeader(ApiKeyFilter.HEADER_NAME) String apiKey) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.addMember(caller, id, request.getUserId(), apiKey));
  }

  @GetMapping("/{id}/members")
  @Operation(summary = "List members of a project")
  public ResponseEntity<Page<MemberResponse>> listMembers(
      @PathVariable Long id, @PageableDefault(size = 20) Pageable pageable) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.ok(projectService.listMembers(caller, id, pageable));
  }

  @DeleteMapping("/{id}/members/{userId}")
  @Operation(summary = "Remove a member from a project (MANAGER, ORG_ADMIN or SUPER_ADMIN)")
  public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
    AuthUser caller = currentUserProvider.require();
    projectService.removeMember(caller, id, userId);
    return ResponseEntity.noContent().build();
  }
}
