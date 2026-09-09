package com.example.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.userservice.dto.OrganizationRequest;
import com.example.userservice.entity.Organization;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.repository.OrganizationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock private OrganizationRepository organizationRepository;

  @InjectMocks private OrganizationService organizationService;

  private User superAdmin() {
    User u = new User();
    u.setId(1L);
    u.setRole(Role.SUPER_ADMIN);
    return u;
  }

  private User orgAdmin(Long orgId) {
    User u = new User();
    u.setId(2L);
    u.setRole(Role.ORG_ADMIN);
    u.setOrganizationId(orgId);
    return u;
  }

  @Test
  void create_rejectsNonSuperAdmin() {
    OrganizationRequest request = new OrganizationRequest();
    request.setName("New Org");

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> organizationService.create(orgAdmin(1L), request));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void create_allowsSuperAdminAndReturnsSavedOrganization() {
    OrganizationRequest request = new OrganizationRequest();
    request.setName("New Org");
    request.setDescription("desc");
    when(organizationRepository.save(any(Organization.class)))
        .thenAnswer(
            inv -> {
              Organization o = inv.getArgument(0);
              o.setId(10L);
              return o;
            });

    Organization result = organizationService.create(superAdmin(), request);

    assertEquals(10L, result.getId());
    assertEquals("New Org", result.getName());
  }

  @Test
  void get_rejectsAccessToForeignOrganization() {
    Organization org = new Organization();
    org.setId(2L);
    when(organizationRepository.findById(2L)).thenReturn(Optional.of(org));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> organizationService.get(orgAdmin(1L), 2L));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void get_allowsOwnOrganization() {
    Organization org = new Organization();
    org.setId(1L);
    org.setName("Acme");
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));

    Organization result = organizationService.get(orgAdmin(1L), 1L);

    assertEquals("Acme", result.getName());
  }

  @Test
  void list_nonSuperAdminSeesOnlyOwnOrganization() {
    Organization org = new Organization();
    org.setId(1L);
    when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));

    var page = organizationService.list(orgAdmin(1L), PageRequest.of(0, 20));

    assertEquals(1, page.getTotalElements());
  }
}
