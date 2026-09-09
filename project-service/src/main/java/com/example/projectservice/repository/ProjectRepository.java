package com.example.projectservice.repository;

import com.example.projectservice.entity.Project;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  Page<Project> findByOrganizationId(Long organizationId, Pageable pageable);

  Page<Project> findByOrganizationIdAndIdIn(
      Long organizationId, Collection<Long> ids, Pageable pageable);
}
