package com.example.taskservice.controller;

import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
import com.example.taskservice.entity.AuthUser;
import com.example.taskservice.entity.TaskStatus;
import com.example.taskservice.security.ApiKeyFilter;
import com.example.taskservice.security.CurrentUserProvider;
import com.example.taskservice.service.TaskService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task management within projects")
public class TaskController {

  private final TaskService taskService;
  private final CurrentUserProvider currentUserProvider;

  public TaskController(TaskService taskService, CurrentUserProvider currentUserProvider) {
    this.taskService = taskService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(
      summary = "Create a task (MANAGER/ORG_ADMIN/SUPER_ADMIN any assignee; MEMBER only their own)")
  public ResponseEntity<TaskResponse> create(
      @Valid @RequestBody TaskRequest request,
      @RequestHeader(ApiKeyFilter.HEADER_NAME) String apiKey) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(taskService.create(caller, request, apiKey));
  }

  @GetMapping
  @Operation(summary = "List tasks (paginated, filterable by projectId/assigneeId/status)")
  public ResponseEntity<Page<TaskResponse>> list(
      @RequestParam(name = "projectId", required = false) Long projectId,
      @RequestParam(name = "assigneeId", required = false) Long assigneeId,
      @RequestParam(name = "status", required = false) TaskStatus status,
      @RequestHeader(ApiKeyFilter.HEADER_NAME) String apiKey,
      @PageableDefault(size = 20) Pageable pageable) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.ok(
        taskService.list(caller, projectId, assigneeId, status, apiKey, pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a task by id")
  public ResponseEntity<TaskResponse> get(
      @PathVariable Long id, @RequestHeader(ApiKeyFilter.HEADER_NAME) String apiKey) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.ok(taskService.get(caller, id, apiKey));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a task (management roles any task; MEMBER only their own)")
  public ResponseEntity<TaskResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody TaskRequest request,
      @RequestHeader(ApiKeyFilter.HEADER_NAME) String apiKey) {
    AuthUser caller = currentUserProvider.require();
    return ResponseEntity.ok(taskService.update(caller, id, request, apiKey));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a task (MANAGER, ORG_ADMIN or SUPER_ADMIN only)")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    AuthUser caller = currentUserProvider.require();
    taskService.delete(caller, id);
    return ResponseEntity.noContent().build();
  }
}
