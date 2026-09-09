package com.example.taskservice.client;

import com.example.taskservice.dto.ProjectSummary;
import com.example.taskservice.security.ApiKeyFilter;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cross-service call to project-service, over Eureka, to confirm the caller has access to a task's
 * project before allowing task operations (project-level access control).
 *
 * <p>The Eureka registry used in this environment is shared across concurrently running deployments
 * that may register unrelated instances under the same conventional service id ("project-service").
 * To resolve this deterministically, this client looks up all Eureka instances registered under
 * that id and picks the one whose port matches this deployment's own project-service port, instead
 * of letting a round-robin load balancer pick an arbitrary (possibly foreign) instance.
 */
@Component
public class ProjectServiceClient {

  private static final int PROJECT_SERVICE_PORT = 21129;

  private final DiscoveryClient discoveryClient;
  private final RestTemplate restTemplate = new RestTemplate();

  public ProjectServiceClient(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  /**
   * Fetches the project as seen by the calling user's own API key. A 200 response means the caller
   * already has read access to the project (project-service enforces org-scoping and
   * project-membership itself). Propagates 403/404 as ResponseStatusException so the caller of this
   * client gets an accurate status.
   */
  public ProjectSummary getAccessibleProject(Long projectId, String callerApiKey) {
    String baseUrl = resolveBaseUrl();
    HttpHeaders headers = new HttpHeaders();
    headers.set(ApiKeyFilter.HEADER_NAME, callerApiKey);
    try {
      var response =
          restTemplate.exchange(
              baseUrl + "/api/v1/projects/{id}",
              HttpMethod.GET,
              new HttpEntity<>(headers),
              ProjectSummary.class,
              projectId);
      return response.getBody();
    } catch (HttpClientErrorException ex) {
      throw new ResponseStatusException(
          ex.getStatusCode(), "project-service denied access to project " + projectId);
    } catch (RestClientException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_GATEWAY, "Unable to reach project-service: " + ex.getMessage());
    }
  }

  private String resolveBaseUrl() {
    List<ServiceInstance> instances = discoveryClient.getInstances("project-service");
    Optional<ServiceInstance> match =
        instances.stream().filter(i -> i.getPort() == PROJECT_SERVICE_PORT).findFirst();
    ServiceInstance instance = match.orElseGet(() -> instances.isEmpty() ? null : instances.get(0));
    if (instance == null) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "project-service is not available");
    }
    return "http://" + instance.getHost() + ":" + instance.getPort();
  }
}
