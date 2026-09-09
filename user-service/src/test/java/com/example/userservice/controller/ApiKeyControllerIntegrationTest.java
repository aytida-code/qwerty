package com.example.userservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.userservice.dto.ApiKeyCreateRequest;
import com.example.userservice.dto.ApiKeyResponse;
import com.example.userservice.entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiKeyControllerIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Value("${app.secret.admin-api-key}")
  private String adminKey;

  private Long createdId;

  private HttpHeaders adminHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Admin-Key", adminKey);
    return headers;
  }

  @AfterEach
  void cleanup() {
    if (createdId != null) {
      restTemplate.exchange(
          "/api/v1/api-keys/" + createdId,
          HttpMethod.DELETE,
          new HttpEntity<>(adminHeaders()),
          Void.class);
      createdId = null;
    }
  }

  @Test
  void create_deniedWithoutValidAdminKey() {
    ApiKeyCreateRequest request = new ApiKeyCreateRequest();
    request.setName("Should Fail");
    request.setEmail("should-fail@example.com");

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Admin-Key", "totally-wrong-key");
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/api-keys", HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void createListRevoke_fullLifecycle() {
    ApiKeyCreateRequest request = new ApiKeyCreateRequest();
    request.setName("Integration Test User");
    request.setEmail("integration-test-user@example.com");
    request.setOrganizationId(1L);
    request.setRole(Role.VIEWER);

    ResponseEntity<ApiKeyResponse> createResponse =
        restTemplate.exchange(
            "/api/v1/api-keys",
            HttpMethod.POST,
            new HttpEntity<>(request, adminHeaders()),
            ApiKeyResponse.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    assertNotNull(createResponse.getBody());
    assertNotNull(createResponse.getBody().getApiKey());
    createdId = createResponse.getBody().getId();

    ResponseEntity<String> listResponse =
        restTemplate.exchange(
            "/api/v1/api-keys", HttpMethod.GET, new HttpEntity<>(adminHeaders()), String.class);
    assertEquals(HttpStatus.OK, listResponse.getStatusCode());

    String issuedKey = createResponse.getBody().getApiKey();
    HttpHeaders callerHeaders = new HttpHeaders();
    callerHeaders.set("X-API-Key", issuedKey);
    ResponseEntity<String> selfLookup =
        restTemplate.exchange(
            "/api/v1/users/" + createdId,
            HttpMethod.GET,
            new HttpEntity<>(callerHeaders),
            String.class);
    assertEquals(HttpStatus.OK, selfLookup.getStatusCode());

    ResponseEntity<Void> revokeResponse =
        restTemplate.exchange(
            "/api/v1/api-keys/" + createdId,
            HttpMethod.DELETE,
            new HttpEntity<>(adminHeaders()),
            Void.class);
    assertEquals(HttpStatus.NO_CONTENT, revokeResponse.getStatusCode());

    // the revoked key should no longer authenticate.
    ResponseEntity<String> afterRevoke =
        restTemplate.exchange(
            "/api/v1/users/" + createdId,
            HttpMethod.GET,
            new HttpEntity<>(callerHeaders),
            String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, afterRevoke.getStatusCode());

    createdId = null;
  }
}
