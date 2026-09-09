package com.example.userservice.controller;

import com.example.userservice.dto.ApiKeyCreateRequest;
import com.example.userservice.dto.ApiKeyResponse;
import com.example.userservice.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/api-keys")
@Tag(name = "API Keys", description = "Admin-gated issuance/listing/revocation of user API keys")
public class ApiKeyController {

  private final ApiKeyService apiKeyService;

  @Value("${app.secret.admin-api-key}")
  private String configuredAdminKey;

  public ApiKeyController(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @PostMapping
  @Operation(summary = "Create a new user + API key (admin only)")
  public ResponseEntity<ApiKeyResponse> create(
      @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
      @Valid @RequestBody ApiKeyCreateRequest request) {
    checkAdmin(adminKey);
    return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.create(request));
  }

  @GetMapping
  @Operation(summary = "List all issued API keys / users (admin only)")
  public ResponseEntity<Page<ApiKeyResponse>> list(
      @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
      @PageableDefault(size = 20) Pageable pageable) {
    checkAdmin(adminKey);
    return ResponseEntity.ok(apiKeyService.list(pageable));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Revoke an API key / deactivate a user (admin only)")
  public ResponseEntity<Void> revoke(
      @RequestHeader(value = "X-Admin-Key", required = false) String adminKey,
      @PathVariable Long id) {
    checkAdmin(adminKey);
    apiKeyService.revoke(id);
    return ResponseEntity.noContent().build();
  }

  private void checkAdmin(String adminKey) {
    if (adminKey == null
        || configuredAdminKey == null
        || configuredAdminKey.isBlank()
        || !MessageDigest.isEqual(
            adminKey.getBytes(StandardCharsets.UTF_8),
            configuredAdminKey.getBytes(StandardCharsets.UTF_8))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin key");
    }
  }
}
