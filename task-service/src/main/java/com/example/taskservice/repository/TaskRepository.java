package com.example.taskservice.repository;

import com.example.taskservice.entity.Task;
import com.example.taskservice.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

  Page<Task> findByOrganizationId(Long organizationId, Pageable pageable);

  Page<Task> findByOrganizationIdAndProjectId(
      Long organizationId, Long projectId, Pageable pageable);

  Page<Task> findByOrganizationIdAndAssigneeId(
      Long organizationId, Long assigneeId, Pageable pageable);

  Page<Task> findByOrganizationIdAndStatus(
      Long organizationId, TaskStatus status, Pageable pageable);

  Page<Task> findByOrganizationIdAndProjectIdAndAssigneeId(
      Long organizationId, Long projectId, Long assigneeId, Pageable pageable);

  Page<Task> findByProjectId(Long projectId, Pageable pageable);

  Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

  Page<Task> findByStatus(TaskStatus status, Pageable pageable);

  Page<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId, Pageable pageable);
}
