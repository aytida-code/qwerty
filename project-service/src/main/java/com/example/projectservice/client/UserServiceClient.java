package com.example.projectservice.client;

import com.example.projectservice.dto.UserSummary;
import com.example.projectservice.security.ApiKeyFilter;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cross-service call to user-service, over Eureka, to validate a target user's
 * existence/organization before assigning them to a project.
 *
 * <p>The Eureka registry used in this environment is shared across concurrently running deployments
 * that may register unrelated instances under the same conventional service id ("user-service"). To
 * resolve this deterministically, this client looks up all Eureka instances registered under that
 * id and picks the one whose port matches this deployment's own user-service port, instead of
 * letting a round-robin load balancer pick an arbitrary (possibly foreign) instance.
 */
@Component
public class UserServiceClient {

  private static final int USER_SERVICE_PORT = 27542;

  private final DiscoveryClient discoveryClient;
  private final RestTemplate restTemplate = new RestTemplate();

  public UserServiceClient(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  public UserSummary getUser(Long userId, String callerApiKey) {
    String baseUrl = resolveBaseUrl();
    HttpHeaders headers = new HttpHeaders();
    headers.set(ApiKeyFilter.HEADER_NAME, callerApiKey);
    try {
      var response =
          restTemplate.exchange(
              baseUrl + "/api/v1/users/{id}",
              HttpMethod.GET,
              new HttpEntity<>(headers),
              UserSummary.class,
              userId);
      return response.getBody();
    } catch (RestClientException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Unable to validate user " + userId + " via user-service: " + ex.getMessage());
    }
  }

  private String resolveBaseUrl() {
    List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
    Optional<ServiceInstance> match =
        instances.stream().filter(i -> i.getPort() == USER_SERVICE_PORT).findFirst();
    ServiceInstance instance = match.orElseGet(() -> instances.isEmpty() ? null : instances.get(0));
    if (instance == null) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "user-service is not available");
    }
    return "http://" + instance.getHost() + ":" + instance.getPort();
  }
}
