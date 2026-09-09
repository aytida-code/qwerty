package com.example.taskservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
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
class TaskControllerIntegrationTest {

  private static final String MANAGER_KEY = "seed-manager-key-0001";
  private static final String MEMBER_KEY = "seed-member-key-0001";
  private static final String VIEWER_KEY = "seed-viewer-key-0001";

  @Autowired private TestRestTemplate restTemplate;

  private Long createdTaskId;

  private HttpHeaders headersFor(String apiKey) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-API-Key", apiKey);
    return headers;
  }

  @AfterEach
  void cleanup() {
    if (createdTaskId != null) {
      restTemplate.exchange(
          "/api/v1/tasks/" + createdTaskId,
          HttpMethod.DELETE,
          new HttpEntity<>(headersFor(MANAGER_KEY)),
          Void.class);
      createdTaskId = null;
    }
  }

  /**
   * Full CRUD lifecycle. Creating/reading a task calls the already-running project-service (via
   * ProjectServiceClient/DiscoveryClient) to confirm project-level access — a real inter-service
   * call, not mocked.
   */
  @Test
  void fullCrudLifecycle() {
    TaskRequest createRequest = new TaskRequest();
    createRequest.setProjectId(1L);
    createRequest.setTitle("IT Task");
    createRequest.setDescription("Created by integration test");

    ResponseEntity<TaskResponse> createResponse =
        restTemplate.exchange(
            "/api/v1/tasks",
            HttpMethod.POST,
            new HttpEntity<>(createRequest, headersFor(MANAGER_KEY)),
            TaskResponse.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    assertNotNull(createResponse.getBody());
    createdTaskId = createResponse.getBody().getId();
    assertEquals(1L, createResponse.getBody().getOrganizationId());

    ResponseEntity<TaskResponse> getResponse =
        restTemplate.exchange(
            "/api/v1/tasks/" + createdTaskId,
            HttpMethod.GET,
            new HttpEntity<>(headersFor(MANAGER_KEY)),
            TaskResponse.class);
    assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    assertEquals("IT Task", getResponse.getBody().getTitle());

    TaskRequest updateRequest = new TaskRequest();
    updateRequest.setProjectId(1L);
    updateRequest.setTitle("IT Task Updated");
    ResponseEntity<TaskResponse> updateResponse =
        restTemplate.exchange(
            "/api/v1/tasks/" + createdTaskId,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headersFor(MANAGER_KEY)),
            TaskResponse.class);
    assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
    assertEquals("IT Task Updated", updateResponse.getBody().getTitle());

    ResponseEntity<Void> deleteResponse =
        restTemplate.exchange(
            "/api/v1/tasks/" + createdTaskId,
            HttpMethod.DELETE,
            new HttpEntity<>(headersFor(MANAGER_KEY)),
            Void.class);
    assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

    ResponseEntity<String> getAfterDelete =
        restTemplate.exchange(
            "/api/v1/tasks/" + createdTaskId,
            HttpMethod.GET,
            new HttpEntity<>(headersFor(MANAGER_KEY)),
            String.class);
    assertEquals(HttpStatus.NOT_FOUND, getAfterDelete.getStatusCode());

    createdTaskId = null;
  }

  @Test
  void create_deniedForViewer() {
    TaskRequest createRequest = new TaskRequest();
    createRequest.setProjectId(1L);
    createRequest.setTitle("Should fail");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/tasks",
            HttpMethod.POST,
            new HttpEntity<>(createRequest, headersFor(VIEWER_KEY)),
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void create_deniedWhenProjectNotAccessible() {
    // Project 2 ("Mobile App") has no members seeded, so a MEMBER has no access
    // to it — project-service's own access check must deny this, proving the
    // real inter-service call happened (not a local no-op).
    TaskRequest createRequest = new TaskRequest();
    createRequest.setProjectId(2L);
    createRequest.setTitle("Should be denied by project-service");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/tasks",
            HttpMethod.POST,
            new HttpEntity<>(createRequest, headersFor(MEMBER_KEY)),
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void memberCanCreateAndUpdateOwnTaskOnly() {
    TaskRequest createRequest = new TaskRequest();
    createRequest.setProjectId(1L);
    createRequest.setTitle("Member's own task");
    ResponseEntity<TaskResponse> createResponse =
        restTemplate.exchange(
            "/api/v1/tasks",
            HttpMethod.POST,
            new HttpEntity<>(createRequest, headersFor(MEMBER_KEY)),
            TaskResponse.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    createdTaskId = createResponse.getBody().getId();
    assertEquals(4L, createResponse.getBody().getAssigneeId());

    TaskRequest updateRequest = new TaskRequest();
    updateRequest.setProjectId(1L);
    updateRequest.setTitle("Updated by owner");
    ResponseEntity<TaskResponse> updateResponse =
        restTemplate.exchange(
            "/api/v1/tasks/" + createdTaskId,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest, headersFor(MEMBER_KEY)),
            TaskResponse.class);
    assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    ResponseEntity<String> deleteAttempt =
        restTemplate.exchange(
            "/api/v1/tasks/" + createdTaskId,
            HttpMethod.DELETE,
            new HttpEntity<>(headersFor(MEMBER_KEY)),
            String.class);
    assertEquals(HttpStatus.FORBIDDEN, deleteAttempt.getStatusCode());

    // manager cleans it up instead.
    restTemplate.exchange(
        "/api/v1/tasks/" + createdTaskId,
        HttpMethod.DELETE,
        new HttpEntity<>(headersFor(MANAGER_KEY)),
        Void.class);
    createdTaskId = null;
  }
}
