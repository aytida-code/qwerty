package com.example.projectservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.projectservice.client.UserServiceClient;
import com.example.projectservice.dto.ProjectRequest;
import com.example.projectservice.dto.ProjectResponse;
import com.example.projectservice.dto.UserSummary;
import com.example.projectservice.entity.AuthUser;
import com.example.projectservice.entity.Project;
import com.example.projectservice.entity.Role;
import com.example.projectservice.repository.ProjectMemberRepository;
import com.example.projectservice.repository.ProjectRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock private ProjectRepository projectRepository;

  @Mock private ProjectMemberRepository memberRepository;

  @Mock private UserServiceClient userServiceClient;

  @InjectMocks private ProjectService projectService;

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

  @Test
  void create_rejectsMemberRole() throws Exception {
    AuthUser member = authUser(4L, Role.MEMBER, 1L);
    ProjectRequest request = new ProjectRequest();
    request.setName("Test");

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> projectService.create(member, request));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void create_succeedsForManagerAndAddsCallerAsMember() throws Exception {
    AuthUser manager = authUser(3L, Role.MANAGER, 1L);
    ProjectRequest request = new ProjectRequest();
    request.setName("New Project");
    when(projectRepository.save(any(Project.class)))
        .thenAnswer(
            inv -> {
              Project p = inv.getArgument(0);
              p.setId(100L);
              return p;
            });

    ProjectResponse response = projectService.create(manager, request);

    assertEquals(100L, response.getId());
    assertEquals(1L, response.getOrganizationId());
  }

  @Test
  void get_rejectsMemberNotAssignedToProject() throws Exception {
    AuthUser member = authUser(4L, Role.MEMBER, 1L);
    Project project = new Project();
    project.setId(1L);
    project.setOrganizationId(1L);
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(memberRepository.existsByProjectIdAndUserId(1L, 4L)).thenReturn(false);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> projectService.get(member, 1L));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void get_allowsAssignedMember() throws Exception {
    AuthUser member = authUser(4L, Role.MEMBER, 1L);
    Project project = new Project();
    project.setId(1L);
    project.setOrganizationId(1L);
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    when(memberRepository.existsByProjectIdAndUserId(1L, 4L)).thenReturn(true);

    ProjectResponse response = projectService.get(member, 1L);

    assertEquals(1L, response.getId());
  }

  @Test
  void addMember_rejectsUserFromDifferentOrganization() throws Exception {
    AuthUser manager = authUser(3L, Role.MANAGER, 1L);
    Project project = new Project();
    project.setId(1L);
    project.setOrganizationId(1L);
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    UserSummary foreignUser = new UserSummary();
    foreignUser.setId(50L);
    foreignUser.setOrganizationId(2L);
    when(userServiceClient.getUser(eq(50L), any())).thenReturn(foreignUser);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> projectService.addMember(manager, 1L, 50L, "some-key"));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void addMember_succeedsForSameOrganizationUser() throws Exception {
    AuthUser manager = authUser(3L, Role.MANAGER, 1L);
    Project project = new Project();
    project.setId(1L);
    project.setOrganizationId(1L);
    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
    UserSummary sameOrgUser = new UserSummary();
    sameOrgUser.setId(5L);
    sameOrgUser.setOrganizationId(1L);
    when(userServiceClient.getUser(eq(5L), any())).thenReturn(sameOrgUser);
    when(memberRepository.existsByProjectIdAndUserId(1L, 5L)).thenReturn(false);
    when(memberRepository.save(any()))
        .thenAnswer(
            inv -> {
              var m = inv.getArgument(0);
              return m;
            });

    var response = projectService.addMember(manager, 1L, 5L, "some-key");

    assertEquals(5L, response.getUserId());
  }
}
