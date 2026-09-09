INSERT INTO organizations (id, name, description, created_at)
VALUES (1, 'Acme Corp', 'Seed organization', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO organizations (id, name, description, created_at)
VALUES (2, 'Globex Inc', 'Second seed organization', now())
ON CONFLICT (id) DO NOTHING;

-- raw keys (for local/dev testing only):
--   seed-super-admin-key-0001 -> SUPER_ADMIN, no organization
--   seed-org-admin-key-0001   -> ORG_ADMIN, org 1
--   seed-manager-key-0001     -> MANAGER, org 1
--   seed-member-key-0001      -> MEMBER, org 1
--   seed-viewer-key-0001      -> VIEWER, org 1
INSERT INTO users (id, name, email, organization_id, role, key_hash, active, created_at)
VALUES (1, 'Root Super Admin', 'super.admin@example.com', NULL, 'SUPER_ADMIN',
        'b0b0b30efbecd9691bc42dc6463e23f30e66ea2c1d43960ceb951d4fb6794d28', true, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, name, email, organization_id, role, key_hash, active, created_at)
VALUES (2, 'Acme Org Admin', 'org.admin@acme.example.com', 1, 'ORG_ADMIN',
        'b0e28b087f9f892946e2fc912b7b4916f83d71a164bb5467f6d663f0dcee1f1a', true, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, name, email, organization_id, role, key_hash, active, created_at)
VALUES (3, 'Acme Manager', 'manager@acme.example.com', 1, 'MANAGER',
        'c9dae8c2235209f610d7e4feb4363ad7920478eeda5be78bb33399c567255774', true, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, name, email, organization_id, role, key_hash, active, created_at)
VALUES (4, 'Acme Member', 'member@acme.example.com', 1, 'MEMBER',
        '8c71c8ab11b6233e15d2c4b2019cdd653b65621c3cf17c5d347a0238c83e7059', true, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, name, email, organization_id, role, key_hash, active, created_at)
VALUES (5, 'Acme Viewer', 'viewer@acme.example.com', 1, 'VIEWER',
        '1da1abf6fc0c27f415dbd930527a5071da33ebff676039ae42ff9d67de8d1931', true, now())
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('organizations_id_seq', (SELECT MAX(id) FROM organizations));
