package com.example.taskservice.security;

import com.example.taskservice.entity.AuthUser;
import com.example.taskservice.repository.AuthUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates the X-API-Key header against the shared "users" table (owned by user-service) via the
 * read-only AuthUser projection. This service does not issue or manage keys — it only authenticates
 * callers.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

  public static final String HEADER_NAME = "X-API-Key";

  private final AuthUserRepository authUserRepository;

  public ApiKeyFilter(AuthUserRepository authUserRepository) {
    this.authUserRepository = authUserRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String apiKey = request.getHeader(HEADER_NAME);
    if (apiKey != null && !apiKey.isBlank()) {
      String hash = hash(apiKey);
      Optional<AuthUser> userOpt = authUserRepository.findByKeyHash(hash);
      if (userOpt.isPresent() && userOpt.get().isActive()) {
        AuthUser user = userOpt.get();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }

  public static String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : hashed) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
