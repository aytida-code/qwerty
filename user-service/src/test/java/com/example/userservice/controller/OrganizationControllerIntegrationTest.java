package com.example.userservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.userservice.dto.OrganizationRequest;
import com.example.userservice.entity.Organization;
import org.junit.jupiter.api.AfterEach;
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
class OrganizationControllerIntegrationTest {

  private static final String SUPER_ADMIN_KEY = "seed-super-admin-key-0001";
  private static final String VIEWER_KEY = "seed-viewer-key-0001";

  @Autowired private TestRestTemplate restTemplate;

  private Long createdId;

  private HttpHeaders headersFor(String apiKey) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", apiKey);
    return headers;
  }

  @AfterEach
  void cleanup() {
    if (createdId != null) {
      restTemplate.exchange(
          "/api/v1/organizations/" + createdId,
          HttpMethod.DELETE,
          new HttpEntity<>(headersFor(SUPER_ADMIN_KEY)),
          Void.class);
      createdId = null;
    }
  }

  @Test
  void fullCrudLifecycle() {
    OrganizationRequest createRequest = new OrganizationRequest();
    createRequest.setName("Integration Test Org");
    createRequest.setDescription("Created by integration test");

    ResponseEntity<Organization> createResponse =
        restTemplate.exchange(
            "/api/v1/organizations",
            HttpMethod.POST,
            new HttpEntity<>(createRequest, headersFor(SUPER_ADMIN_KEY)),
            Organization.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    assertNotNull(createResponse.getBody());
    createdId = createResponse.getBody().getId();
    assertNotNull(createdId);
    assertEquals("Integration Test Org", createResponse.getBody().getName());

    ResponseEntity<Organization> getResponse =
        restTemplate.exchange(
            "/api/v1/organizations/" + createdId,
            HttpMethod.GET,
            new HttpEntity<>(headersFor(SUPER_ADMIN_KEY)),
            Organization.class);
    assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    assertEquals("Integration Test Org", getResponse.getBody().getName());

    OrganizationRequest updateRequest = new OrganizationRequest();
    updateRequest.setName("Integration Test Org Updated");
    updateRequest.setDescription("Updated description");
    ResponseEntity<Organization> updateResponse =
        restTemplate.exchange(
            "/api/v1/organizations/" + createdId,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headersFor(SUPER_ADMIN_KEY)),
            Organization.class);
    assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
    assertEquals("Integration Test Org Updated", updateResponse.getBody().getName());

    ResponseEntity<Void> deleteResponse =
        restTemplate.exchange(
            "/api/v1/organizations/" + createdId,
            HttpMethod.DELETE,
            new HttpEntity<>(headersFor(SUPER_ADMIN_KEY)),
            Void.class);
    assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

    ResponseEntity<String> getAfterDelete =
        restTemplate.exchange(
            "/api/v1/organizations/" + createdId,
            HttpMethod.GET,
            new HttpEntity<>(headersFor(SUPER_ADMIN_KEY)),
            String.class);
    assertEquals(HttpStatus.NOT_FOUND, getAfterDelete.getStatusCode());

    createdId = null;
  }

  @Test
  void create_deniedForNonSuperAdmin() {
    OrganizationRequest createRequest = new OrganizationRequest();
    createRequest.setName("Should Not Be Created");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/organizations",
            HttpMethod.POST,
            new HttpEntity<>(createRequest, headersFor(VIEWER_KEY)),
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void get_deniedForForeignOrganization() {
    // organization 2 (Globex Inc) is not the viewer's own organization (1)
    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/organizations/2",
            HttpMethod.GET,
            new HttpEntity<>(headersFor(VIEWER_KEY)),
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}
