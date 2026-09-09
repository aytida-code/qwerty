package com.example.taskservice.service;

import com.example.taskservice.client.ProjectServiceClient;
import com.example.taskservice.dto.ProjectSummary;
import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
import com.example.taskservice.entity.AuthUser;
import com.example.taskservice.entity.Role;
import com.example.taskservice.entity.Task;
import com.example.taskservice.entity.TaskStatus;
import com.example.taskservice.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final ProjectServiceClient projectServiceClient;

  public TaskService(TaskRepository taskRepository, ProjectServiceClient projectServiceClient) {
    this.taskRepository = taskRepository;
    this.projectServiceClient = projectServiceClient;
  }

  public TaskResponse create(AuthUser caller, TaskRequest request, String apiKey) {
    if (caller.getRole() == Role.VIEWER) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "VIEWER has read-only access");
    }
    ProjectSummary project =
        projectServiceClient.getAccessibleProject(request.getProjectId(), apiKey);

    Long assigneeId = request.getAssigneeId() != null ? request.getAssigneeId() : caller.getId();
    if (caller.getRole() == Role.MEMBER && !assigneeId.equals(caller.getId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "MEMBER may only create their own tasks");
    }

    Task task = new Task();
    task.setProjectId(project.getId());
    task.setOrganizationId(project.getOrganizationId());
    task.setAssigneeId(assigneeId);
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(request.getStatus());
    task.setDueDate(request.getDueDate());
    task.setCreatedBy(caller.getId());
    return TaskResponse.from(taskRepository.save(task));
  }

  public Page<TaskResponse> list(
      AuthUser caller,
      Long projectId,
      Long assigneeId,
      TaskStatus status,
      String apiKey,
      Pageable pageable) {
    if (projectId != null) {
      projectServiceClient.getAccessibleProject(projectId, apiKey);
    }
    Long orgScope = caller.getRole() == Role.SUPER_ADMIN ? null : caller.getOrganizationId();

    Page<Task> page;
    if (orgScope == null) {
      page = filterWithoutOrg(projectId, assigneeId, status, pageable);
    } else if (projectId != null && assigneeId != null) {
      page =
          taskRepository.findByOrganizationIdAndProjectIdAndAssigneeId(
              orgScope, projectId, assigneeId, pageable);
    } else if (projectId != null) {
      page = taskRepository.findByOrganizationIdAndProjectId(orgScope, projectId, pageable);
    } else if (assigneeId != null) {
      page = taskRepository.findByOrganizationIdAndAssigneeId(orgScope, assigneeId, pageable);
    } else if (status != null) {
      page = taskRepository.findByOrganizationIdAndStatus(orgScope, status, pageable);
    } else {
      page = taskRepository.findByOrganizationId(orgScope, pageable);
    }
    return page.map(TaskResponse::from);
  }

  public TaskResponse get(AuthUser caller, Long id, String apiKey) {
    Task task = findById(id);
    assertOrgScope(caller, task);
    projectServiceClient.getAccessibleProject(task.getProjectId(), apiKey);
    return TaskResponse.from(task);
  }

  public TaskResponse update(AuthUser caller, Long id, TaskRequest request, String apiKey) {
    Task task = findById(id);
    assertOrgScope(caller, task);
    projectServiceClient.getAccessibleProject(task.getProjectId(), apiKey);

    boolean isManagement =
        caller.getRole() == Role.MANAGER
            || caller.getRole() == Role.ORG_ADMIN
            || caller.getRole() == Role.SUPER_ADMIN;
    boolean isOwnTask =
        caller.getRole() == Role.MEMBER
            && task.getAssigneeId() != null
            && task.getAssigneeId().equals(caller.getId());
    if (!isManagement && !isOwnTask) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update this task");
    }

    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(request.getStatus());
    task.setDueDate(request.getDueDate());
    if (isManagement && request.getAssigneeId() != null) {
      task.setAssigneeId(request.getAssigneeId());
    }
    task.setUpdatedAt(java.time.Instant.now());
    return TaskResponse.from(taskRepository.save(task));
  }

  public void delete(AuthUser caller, Long id) {
    Task task = findById(id);
    assertOrgScope(caller, task);
    boolean isManagement =
        caller.getRole() == Role.MANAGER
            || caller.getRole() == Role.ORG_ADMIN
            || caller.getRole() == Role.SUPER_ADMIN;
    if (!isManagement) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only MANAGER, ORG_ADMIN or SUPER_ADMIN may delete tasks");
    }
    taskRepository.deleteById(id);
  }

  private Page<Task> filterWithoutOrg(
      Long projectId, Long assigneeId, TaskStatus status, Pageable pageable) {
    if (projectId != null && assigneeId != null) {
      return taskRepository.findByProjectIdAndAssigneeId(projectId, assigneeId, pageable);
    } else if (projectId != null) {
      return taskRepository.findByProjectId(projectId, pageable);
    } else if (assigneeId != null) {
      return taskRepository.findByAssigneeId(assigneeId, pageable);
    } else if (status != null) {
      return taskRepository.findByStatus(status, pageable);
    }
    return taskRepository.findAll(pageable);
  }

  private void assertOrgScope(AuthUser caller, Task task) {
    if (caller.getRole() != Role.SUPER_ADMIN
        && (caller.getOrganizationId() == null
            || !caller.getOrganizationId().equals(task.getOrganizationId()))) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Cannot access another organization's task");
    }
  }

  private Task findById(Long id) {
    return taskRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
  }
}
