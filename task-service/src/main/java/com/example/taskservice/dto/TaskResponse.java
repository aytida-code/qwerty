package com.example.taskservice.dto;

import com.example.taskservice.entity.Task;
import com.example.taskservice.entity.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;

public class TaskResponse {

  private Long id;
  private Long projectId;
  private Long organizationId;
  private Long assigneeId;
  private String title;
  private String description;
  private TaskStatus status;
  private LocalDate dueDate;
  private Long createdBy;
  private Instant createdAt;
  private Instant updatedAt;

  public static TaskResponse from(Task task) {
    TaskResponse dto = new TaskResponse();
    dto.id = task.getId();
    dto.projectId = task.getProjectId();
    dto.organizationId = task.getOrganizationId();
    dto.assigneeId = task.getAssigneeId();
    dto.title = task.getTitle();
    dto.description = task.getDescription();
    dto.status = task.getStatus();
    dto.dueDate = task.getDueDate();
    dto.createdBy = task.getCreatedBy();
    dto.createdAt = task.getCreatedAt();
    dto.updatedAt = task.getUpdatedAt();
    return dto;
  }

  public Long getId() {
    return id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public Long getOrganizationId() {
    return organizationId;
  }

  public Long getAssigneeId() {
    return assigneeId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
