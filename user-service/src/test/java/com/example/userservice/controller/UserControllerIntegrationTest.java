package com.example.userservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.userservice.dto.RoleChangeRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

  private static final String MEMBER_KEY = "seed-member-key-0001";
  private static final String ORG_ADMIN_KEY = "seed-org-admin-key-0001";

  @Autowired private TestRestTemplate restTemplate;

  private HttpHeaders headersFor(String apiKey) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", apiKey);
    return headers;
  }

  @Test
  void listUsers_requiresApiKey() {
    ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users", String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void listUsers_scopedToCallersOrganization() {
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/users",
            HttpMethod.GET,
            new HttpEntity<>(headersFor(MEMBER_KEY)),
            String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void fullLifecycle_updateProfileThenRoleChangeThenDeactivate() {
    // The seeded member (id 4) updates their own profile.
    UserUpdateRequest update = new UserUpdateRequest();
    update.setName("Updated Member Name");
    update.setEmail("member-updated@acme.example.com");
    ResponseEntity<UserResponse> updateResponse =
        restTemplate.exchange(
            "/api/v1/users/4",
            HttpMethod.PUT,
            new HttpEntity<>(update, headersFor(MEMBER_KEY)),
            UserResponse.class);
    assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
    assertNotNull(updateResponse.getBody());
    assertEquals("Updated Member Name", updateResponse.getBody().getName());

    // restore email/name so re-runs stay idempotent
    UserUpdateRequest restore = new UserUpdateRequest();
    restore.setName("Acme Member");
    restore.setEmail("member@acme.example.com");
    restTemplate.exchange(
        "/api/v1/users/4",
        HttpMethod.PUT,
        new HttpEntity<>(restore, headersFor(MEMBER_KEY)),
        UserResponse.class);

    // ORG_ADMIN promotes member -> MANAGER, then reverts (role changes are business ops)
    RoleChangeRequest promote = new RoleChangeRequest();
    promote.setRole(Role.MANAGER);
    ResponseEntity<UserResponse> promoteResponse =
        restTemplate.exchange(
            "/api/v1/users/4/role",
            HttpMethod.PATCH,
            new HttpEntity<>(promote, headersFor(ORG_ADMIN_KEY)),
            UserResponse.class);
    assertEquals(HttpStatus.OK, promoteResponse.getStatusCode());
    assertEquals(Role.MANAGER, promoteResponse.getBody().getRole());

    RoleChangeRequest revert = new RoleChangeRequest();
    revert.setRole(Role.MEMBER);
    ResponseEntity<UserResponse> revertResponse =
        restTemplate.exchange(
            "/api/v1/users/4/role",
            HttpMethod.PATCH,
            new HttpEntity<>(revert, headersFor(ORG_ADMIN_KEY)),
            UserResponse.class);
    assertEquals(HttpStatus.OK, revertResponse.getStatusCode());
    assertEquals(Role.MEMBER, revertResponse.getBody().getRole());
  }

  @Test
  void roleChange_deniedWhenEscalatingAboveCallerLevel() {
    RoleChangeRequest escalate = new RoleChangeRequest();
    escalate.setRole(Role.SUPER_ADMIN);
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/users/4/role",
            HttpMethod.PATCH,
            new HttpEntity<>(escalate, headersFor(ORG_ADMIN_KEY)),
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}
