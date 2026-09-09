package com.example.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.userservice.dto.ApiKeyCreateRequest;
import com.example.userservice.dto.ApiKeyResponse;
import com.example.userservice.entity.Role;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private ApiKeyService apiKeyService;

  @Test
  void create_rejectsDuplicateEmail() {
    when(userRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new User()));
    ApiKeyCreateRequest request = new ApiKeyCreateRequest();
    request.setName("Dup");
    request.setEmail("dup@example.com");

    assertThrows(ResponseStatusException.class, () -> apiKeyService.create(request));
  }

  @Test
  void create_generatesRawKeyAndStoresOnlyItsHash() {
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            inv -> {
              User u = inv.getArgument(0);
              u.setId(42L);
              return u;
            });
    ApiKeyCreateRequest request = new ApiKeyCreateRequest();
    request.setName("New User");
    request.setEmail("new@example.com");
    request.setOrganizationId(1L);
    request.setRole(Role.MEMBER);

    ApiKeyResponse response = apiKeyService.create(request);

    assertNotNull(response.getApiKey());
    assertEquals(42L, response.getId());
    assertEquals(Role.MEMBER, response.getRole());
  }

  @Test
  void revoke_deactivatesUser() {
    User u = new User();
    u.setId(5L);
    u.setActive(true);
    when(userRepository.findById(5L)).thenReturn(Optional.of(u));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    apiKeyService.revoke(5L);

    assertFalse(u.isActive());
  }

  @Test
  void revoke_throwsWhenUserMissing() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> apiKeyService.revoke(99L));
  }
}
