package com.example.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.userservice.dto.RoleChangeRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  private User orgAdmin;
  private User member;

  @BeforeEach
  void setUp() {
    orgAdmin = user(2L, Role.ORG_ADMIN, 1L);
    member = user(4L, Role.MEMBER, 1L);
  }

  private User user(Long id, Role role, Long orgId) {
    User u = new User();
    u.setId(id);
    u.setName("Name" + id);
    u.setEmail("user" + id + "@example.com");
    u.setRole(role);
    u.setOrganizationId(orgId);
    u.setActive(true);
    return u;
  }

  @Test
  void changeRole_rejectsRoleHigherThanCallerLevel() {
    when(userRepository.findById(4L)).thenReturn(Optional.of(member));
    RoleChangeRequest request = new RoleChangeRequest();
    request.setRole(Role.SUPER_ADMIN);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> userService.changeRole(orgAdmin, 4L, request));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void changeRole_allowsValidPromotionWithinHierarchy() {
    when(userRepository.findById(4L)).thenReturn(Optional.of(member));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    RoleChangeRequest request = new RoleChangeRequest();
    request.setRole(Role.MANAGER);

    UserResponse response = userService.changeRole(orgAdmin, 4L, request);

    assertEquals(Role.MANAGER, response.getRole());
  }

  @Test
  void changeRole_rejectsCrossOrganization() {
    User foreignMember = user(9L, Role.MEMBER, 2L);
    when(userRepository.findById(9L)).thenReturn(Optional.of(foreignMember));
    RoleChangeRequest request = new RoleChangeRequest();
    request.setRole(Role.MANAGER);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> userService.changeRole(orgAdmin, 9L, request));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void get_rejectsAccessToAnotherOrganization() {
    User foreignMember = user(9L, Role.MEMBER, 2L);
    when(userRepository.findById(9L)).thenReturn(Optional.of(foreignMember));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> userService.get(orgAdmin, 9L));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void get_allowsSameOrganization() {
    when(userRepository.findById(4L)).thenReturn(Optional.of(member));

    UserResponse response = userService.get(orgAdmin, 4L);

    assertEquals(4L, response.getId());
  }

  @Test
  void update_allowsSelfUpdate() {
    when(userRepository.findById(4L)).thenReturn(Optional.of(member));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    UserUpdateRequest request = new UserUpdateRequest();
    request.setName("New Name");
    request.setEmail("new@example.com");

    UserResponse response = userService.update(member, 4L, request);

    assertEquals("New Name", response.getName());
    assertEquals("new@example.com", response.getEmail());
  }

  @Test
  void update_rejectsUnrelatedUser() {
    User anotherMember = user(5L, Role.MEMBER, 1L);
    when(userRepository.findById(5L)).thenReturn(Optional.of(anotherMember));
    UserUpdateRequest request = new UserUpdateRequest();
    request.setName("Hacker");
    request.setEmail("hacker@example.com");

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> userService.update(member, 5L, request));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void deactivate_allowedForOrgAdminOfSameOrg() {
    when(userRepository.findById(4L)).thenReturn(Optional.of(member));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    userService.deactivate(orgAdmin, 4L);

    assertEquals(false, member.isActive());
  }
}
