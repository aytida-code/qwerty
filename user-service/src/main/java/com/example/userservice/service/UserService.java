package com.example.userservice.service;

import com.example.userservice.dto.RoleChangeRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Page<UserResponse> list(User caller, Long organizationIdFilter, Pageable pageable) {
    Long orgScope = resolveOrgScope(caller, organizationIdFilter);
    Page<User> page =
        orgScope == null
            ? userRepository.findAll(pageable)
            : userRepository.findByOrganizationId(orgScope, pageable);
    return page.map(UserResponse::from);
  }

  public UserResponse get(User caller, Long id) {
    User target = findById(id);
    assertSameOrgOrSuperAdmin(caller, target.getOrganizationId());
    return UserResponse.from(target);
  }

  public UserResponse update(User caller, Long id, UserUpdateRequest request) {
    User target = findById(id);
    boolean isSelf = target.getId().equals(caller.getId());
    boolean canManage =
        caller.getRole() == Role.SUPER_ADMIN
            || (caller.getRole() == Role.ORG_ADMIN && sameOrg(caller, target));
    if (!isSelf && !canManage) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify this user");
    }
    target.setName(request.getName());
    target.setEmail(request.getEmail());
    return UserResponse.from(userRepository.save(target));
  }

  public UserResponse changeRole(User caller, Long id, RoleChangeRequest request) {
    if (caller.getRole() != Role.SUPER_ADMIN && caller.getRole() != Role.ORG_ADMIN) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only ORG_ADMIN or SUPER_ADMIN may change roles");
    }
    User target = findById(id);
    if (caller.getRole() == Role.ORG_ADMIN) {
      if (!sameOrg(caller, target)) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Cannot manage users outside your organization");
      }
      if (request.getRole().getLevel() > caller.getRole().getLevel()) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Cannot assign a role higher than your own permission level");
      }
    }
    target.setRole(request.getRole());
    return UserResponse.from(userRepository.save(target));
  }

  public void deactivate(User caller, Long id) {
    User target = findById(id);
    boolean canManage =
        caller.getRole() == Role.SUPER_ADMIN
            || (caller.getRole() == Role.ORG_ADMIN && sameOrg(caller, target));
    if (!canManage) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete this user");
    }
    target.setActive(false);
    userRepository.save(target);
  }

  private Long resolveOrgScope(User caller, Long organizationIdFilter) {
    if (caller.getRole() == Role.SUPER_ADMIN) {
      return organizationIdFilter;
    }
    return caller.getOrganizationId();
  }

  private void assertSameOrgOrSuperAdmin(User caller, Long targetOrgId) {
    if (caller.getRole() == Role.SUPER_ADMIN) {
      return;
    }
    if (caller.getOrganizationId() == null || !caller.getOrganizationId().equals(targetOrgId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Cannot access users outside your organization");
    }
  }

  private boolean sameOrg(User a, User b) {
    return a.getOrganizationId() != null && a.getOrganizationId().equals(b.getOrganizationId());
  }

  private User findById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
