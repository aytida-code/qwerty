package com.example.projectservice.repository;

import com.example.projectservice.entity.ProjectMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

  Page<ProjectMember> findByProjectId(Long projectId, Pageable pageable);

  List<ProjectMember> findByProjectId(Long projectId);

  List<ProjectMember> findByUserId(Long userId);

  Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

  boolean existsByProjectIdAndUserId(Long projectId, Long userId);

  void deleteByProjectIdAndUserId(Long projectId, Long userId);
}
