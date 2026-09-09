package com.example.taskservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.taskservice.client.ProjectServiceClient;
import com.example.taskservice.dto.ProjectSummary;
import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
import com.example.taskservice.entity.AuthUser;
import com.example.taskservice.entity.Role;
import com.example.taskservice.entity.Task;
import com.example.taskservice.repository.TaskRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;

  @Mock private ProjectServiceClient projectServiceClient;

  @InjectMocks private TaskService taskService;

  private AuthUser authUser(Long id, Role role, Long orgId) throws Exception {
    AuthUser user = new AuthUser();
    setField(user, "id", id);
    setField(user, "role", role);
    setField(user, "organizationId", orgId);
    setField(user, "active", true);
    return user;
  }

  private void setField(Object target, String name, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(target, value);
  }

  private ProjectSummary project(Long id, Long orgId) {
    ProjectSummary p = new ProjectSummary();
    p.setId(id);
    p.setOrganizationId(orgId);
    return p;
  }

  @Test
  void create_rejectsViewer() throws Exception {
    AuthUser viewer = authUser(5L, Role.VIEWER, 1L);
    TaskRequest request = new TaskRequest();
    request.setProjectId(1L);
    request.setTitle("Task");

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> taskService.create(viewer, request, "key"));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void create_rejectsMemberAssigningSomeoneElse() throws Exception {
    AuthUser member = authUser(4L, Role.MEMBER, 1L);
    when(projectServiceClient.getAccessibleProject(1L, "key")).thenReturn(project(1L, 1L));
    TaskRequest request = new TaskRequest();
    request.setProjectId(1L);
    request.setTitle("Task");
    request.setAssigneeId(3L);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> taskService.create(member, request, "key"));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void create_allowsMemberAssigningSelf() throws Exception {
    AuthUser member = authUser(4L, Role.MEMBER, 1L);
    when(projectServiceClient.getAccessibleProject(1L, "key")).thenReturn(project(1L, 1L));
    when(taskRepository.save(any(Task.class)))
        .thenAnswer(
            inv -> {
              Task t = inv.getArgument(0);
              t.setId(10L);
              return t;
            });
    TaskRequest request = new TaskRequest();
    request.setProjectId(1L);
    request.setTitle("My task");

    TaskResponse response = taskService.create(member, request, "key");

    assertEquals(4L, response.getAssigneeId());
    assertEquals(1L, response.getOrganizationId());
  }

  @Test
  void update_rejectsMemberUpdatingSomeoneElsesTask() throws Exception {
    AuthUser member = authUser(4L, Role.MEMBER, 1L);
    Task task = new Task();
    task.setId(1L);
    task.setOrganizationId(1L);
    task.setProjectId(1L);
    task.setAssigneeId(3L);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(projectServiceClient.getAccessibleProject(1L, "key")).thenReturn(project(1L, 1L));
    TaskRequest request = new TaskRequest();
    request.setProjectId(1L);
    request.setTitle("Hijack");

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> taskService.update(member, 1L, request, "key"));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void delete_rejectsMember() throws Exception {
    AuthUser member = authUser(4L, Role.MEMBER, 1L);
    Task task = new Task();
    task.setId(1L);
    task.setOrganizationId(1L);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> taskService.delete(member, 1L));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void delete_allowsManager() throws Exception {
    AuthUser manager = authUser(3L, Role.MANAGER, 1L);
    Task task = new Task();
    task.setId(1L);
    task.setOrganizationId(1L);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

    taskService.delete(manager, 1L);
    // no exception -> deletion delegated to repository.
  }
}
