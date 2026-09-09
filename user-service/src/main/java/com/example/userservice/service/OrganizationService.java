package com.example.userservice.service;

import com.example.userservice.dto.OrganizationRequest;
import com.example.userservice.entity.Organization;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.repository.OrganizationRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;

  public OrganizationService(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  public Organization create(User caller, OrganizationRequest request) {
    requireSuperAdmin(caller);
    Organization org = new Organization();
    org.setName(request.getName());
    org.setDescription(request.getDescription());
    return organizationRepository.save(org);
  }

  public Page<Organization> list(User caller, Pageable pageable) {
    if (caller.getRole() == Role.SUPER_ADMIN) {
      return organizationRepository.findAll(pageable);
    }
    Organization own = get(caller, caller.getOrganizationId());
    return new PageImpl<>(List.of(own), pageable, 1);
  }

  public Organization get(User caller, Long id) {
    Organization org =
        organizationRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
    if (caller.getRole() != Role.SUPER_ADMIN
        && (caller.getOrganizationId() == null
            || !caller.getOrganizationId().equals(org.getId()))) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access another organization");
    }
    return org;
  }

  public Organization update(User caller, Long id, OrganizationRequest request) {
    requireSuperAdmin(caller);
    Organization org =
        organizationRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
    org.setName(request.getName());
    org.setDescription(request.getDescription());
    return organizationRepository.save(org);
  }

  public void delete(User caller, Long id) {
    requireSuperAdmin(caller);
    if (!organizationRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
    }
    organizationRepository.deleteById(id);
  }

  private void requireSuperAdmin(User caller) {
    if (caller.getRole() != Role.SUPER_ADMIN) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only SUPER_ADMIN may manage organizations");
    }
  }
}
