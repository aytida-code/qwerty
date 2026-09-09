package com.example.projectservice.service;

import com.example.projectservice.client.UserServiceClient;
import com.example.projectservice.dto.MemberResponse;
import com.example.projectservice.dto.ProjectRequest;
import com.example.projectservice.dto.ProjectResponse;
import com.example.projectservice.dto.UserSummary;
import com.example.projectservice.entity.AuthUser;
import com.example.projectservice.entity.Project;
import com.example.projectservice.entity.ProjectMember;
import com.example.projectservice.entity.Role;
import com.example.projectservice.repository.ProjectMemberRepository;
import com.example.projectservice.repository.ProjectRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectMemberRepository memberRepository;
  private final UserServiceClient userServiceClient;

  public ProjectService(
      ProjectRepository projectRepository,
      ProjectMemberRepository memberRepository,
      UserServiceClient userServiceClient) {
    this.projectRepository = projectRepository;
    this.memberRepository = memberRepository;
    this.userServiceClient = userServiceClient;
  }

  public ProjectResponse create(AuthUser caller, ProjectRequest request) {
    if (caller.getRole().getLevel() < Role.MANAGER.getLevel()) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only MANAGER, ORG_ADMIN or SUPER_ADMIN may create projects");
    }
    Long orgId =
        caller.getRole() == Role.SUPER_ADMIN
            ? request.getOrganizationId()
            : caller.getOrganizationId();
    if (orgId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "organizationId is required");
    }
    Project project = new Project();
    project.setName(request.getName());
    project.setDescription(request.getDescription());
    project.setOrganizationId(orgId);
    project.setCreatedBy(caller.getId());
    Project saved = projectRepository.save(project);

    ProjectMember member = new ProjectMember();
    member.setProjectId(saved.getId());
    member.setUserId(caller.getId());
    memberRepository.save(member);

    return ProjectResponse.from(saved);
  }

  public Page<ProjectResponse> list(AuthUser caller, Pageable pageable) {
    Page<Project> page;
    if (caller.getRole() == Role.SUPER_ADMIN) {
      page = projectRepository.findAll(pageable);
    } else if (caller.getRole() == Role.ORG_ADMIN || caller.getRole() == Role.MANAGER) {
      page = projectRepository.findByOrganizationId(caller.getOrganizationId(), pageable);
    } else {
      List<Long> projectIds =
          memberRepository.findByUserId(caller.getId()).stream()
              .map(ProjectMember::getProjectId)
              .toList();
      if (projectIds.isEmpty()) {
        page = new PageImpl<>(List.of(), pageable, 0);
      } else {
        page =
            projectRepository.findByOrganizationIdAndIdIn(
                caller.getOrganizationId(), projectIds, pageable);
      }
    }
    return page.map(ProjectResponse::from);
  }

  public ProjectResponse get(AuthUser caller, Long id) {
    Project project = findById(id);
    assertReadAccess(caller, project);
    return ProjectResponse.from(project);
  }

  public ProjectResponse update(AuthUser caller, Long id, ProjectRequest request) {
    Project project = findById(id);
    assertManageAccess(caller, project);
    project.setName(request.getName());
    project.setDescription(request.getDescription());
    return ProjectResponse.from(projectRepository.save(project));
  }

  public void delete(AuthUser caller, Long id) {
    Project project = findById(id);
    assertManageAccess(caller, project);
    memberRepository.findByProjectId(id).forEach(m -> memberRepository.deleteById(m.getId()));
    projectRepository.deleteById(id);
  }

  public MemberResponse addMember(
      AuthUser caller, Long projectId, Long userId, String callerApiKey) {
    Project project = findById(projectId);
    assertManageAccess(caller, project);

    UserSummary target = userServiceClient.getUser(userId, callerApiKey);
    if (target == null
        || target.getOrganizationId() == null
        || !target.getOrganizationId().equals(project.getOrganizationId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Target user does not belong to this project's organization");
    }
    if (memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "User is already a member of this project");
    }
    ProjectMember member = new ProjectMember();
    member.setProjectId(projectId);
    member.setUserId(userId);
    return MemberResponse.from(memberRepository.save(member));
  }

  public Page<MemberResponse> listMembers(AuthUser caller, Long projectId, Pageable pageable) {
    Project project = findById(projectId);
    assertReadAccess(caller, project);
    return memberRepository.findByProjectId(projectId, pageable).map(MemberResponse::from);
  }

  public void removeMember(AuthUser caller, Long projectId, Long userId) {
    Project project = findById(projectId);
    assertManageAccess(caller, project);
    if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found");
    }
    memberRepository.deleteByProjectIdAndUserId(projectId, userId);
  }

  public boolean hasAccess(Long userId, Long projectId) {
    return memberRepository.existsByProjectIdAndUserId(projectId, userId);
  }

  private Project findById(Long id) {
    return projectRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
  }

  private void assertReadAccess(AuthUser caller, Project project) {
    if (caller.getRole() != Role.SUPER_ADMIN
        && !project.getOrganizationId().equals(caller.getOrganizationId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Cannot access another organization's project");
    }
    if (caller.getRole() == Role.MEMBER || caller.getRole() == Role.VIEWER) {
      if (!memberRepository.existsByProjectIdAndUserId(project.getId(), caller.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this project");
      }
    }
  }

  private void assertManageAccess(AuthUser caller, Project project) {
    if (caller.getRole() == Role.SUPER_ADMIN) {
      return;
    }
    if (!project.getOrganizationId().equals(caller.getOrganizationId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Cannot manage another organization's project");
    }
    if (caller.getRole() != Role.ORG_ADMIN && caller.getRole() != Role.MANAGER) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only MANAGER, ORG_ADMIN or SUPER_ADMIN may manage projects");
    }
  }
}
