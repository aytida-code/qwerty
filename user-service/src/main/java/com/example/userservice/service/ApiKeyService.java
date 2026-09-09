package com.example.userservice.service;

import com.example.userservice.dto.ApiKeyCreateRequest;
import com.example.userservice.dto.ApiKeyResponse;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.ApiKeyFilter;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApiKeyService {

  private final UserRepository userRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  public ApiKeyService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public ApiKeyResponse create(ApiKeyCreateRequest request) {
    userRepository
        .findByEmail(request.getEmail())
        .ifPresent(
            u -> {
              throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
            });
    String rawKey = generateKey();
    User user = new User();
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    user.setOrganizationId(request.getOrganizationId());
    user.setRole(request.getRole());
    user.setKeyHash(ApiKeyFilter.hash(rawKey));
    user.setActive(true);
    User saved = userRepository.save(user);

    ApiKeyResponse response = toResponse(saved);
    response.setApiKey(rawKey);
    return response;
  }

  public Page<ApiKeyResponse> list(Pageable pageable) {
    return userRepository.findAll(pageable).map(this::toResponse);
  }

  public void revoke(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    user.setActive(false);
    userRepository.save(user);
  }

  private String generateKey() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private ApiKeyResponse toResponse(User user) {
    ApiKeyResponse dto = new ApiKeyResponse();
    dto.setId(user.getId());
    dto.setName(user.getName());
    dto.setEmail(user.getEmail());
    dto.setOrganizationId(user.getOrganizationId());
    dto.setRole(user.getRole());
    dto.setActive(user.isActive());
    dto.setCreatedAt(user.getCreatedAt());
    return dto;
  }
}
