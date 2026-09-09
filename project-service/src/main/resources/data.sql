-- Seed projects for organization 1 (Acme Corp), created by user id 3 (manager@acme.example.com)
INSERT INTO projects (id, name, description, organization_id, created_by, status, created_at)
VALUES (1, 'Website Revamp', 'Redesign the marketing site', 1, 3, 'ACTIVE', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO projects (id, name, description, organization_id, created_by, status, created_at)
VALUES (2, 'Mobile App', 'New mobile application', 1, 3, 'ACTIVE', now())
ON CONFLICT (id) DO NOTHING;

-- Membership: manager (3) and member (4) belong to project 1
INSERT INTO project_members (id, project_id, user_id, created_at)
VALUES (1, 1, 3, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_members (id, project_id, user_id, created_at)
VALUES (2, 1, 4, now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('projects_id_seq', (SELECT MAX(id) FROM projects));
SELECT setval('project_members_id_seq', (SELECT MAX(id) FROM project_members));
