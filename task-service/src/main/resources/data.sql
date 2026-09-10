ALTER TABLE tasks MODIFY id BIGINT NOT NULL AUTO_INCREMENT;

-- Seed tasks on project 1 (org 1), assigned to member (user id 4) / manager (user id 3)
INSERT INTO tasks (id, project_id, organization_id, assignee_id, title, description, status, due_date, created_by, created_at, updated_at)
VALUES (1, 1, 1, 4, 'Design homepage mockup', 'Create initial mockup for approval', 'TODO', NULL, 3, now(), now())
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tasks (id, project_id, organization_id, assignee_id, title, description, status, due_date, created_by, created_at, updated_at)
VALUES (2, 1, 1, 3, 'Set up CI pipeline', 'Configure build pipeline for the project', 'IN_PROGRESS', NULL, 3, now(), now())
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tasks (id, project_id, organization_id, assignee_id, title, description, status, due_date, created_by, created_at, updated_at)
VALUES (3, 1, 1, 4, 'Write onboarding docs', 'Document onboarding process', 'DONE', NULL, 4, now(), now())
ON DUPLICATE KEY UPDATE id = id;
