-- ============================================================
-- Multi-Tenant Notification System — Seed Data
-- Run this ONCE against your PostgreSQL notification_db
-- ============================================================

-- Tenants
INSERT INTO tenant (name, api_key) VALUES
  ('Acme Corp',    'acme-api-key-abc123'),
  ('Globex Inc',   'globex-api-key-xyz789')
ON CONFLICT (api_key) DO NOTHING;

-- Users (tenant 1 = Acme, tenant 2 = Globex)
INSERT INTO app_user (name, email, tenant_id) VALUES
  ('Alice Martin',  'alice@acme.com',    1),
  ('Bob Johnson',   'bob@acme.com',      1),
  ('Carol White',   'carol@globex.com',  2),
  ('David Brown',   'david@globex.com',  2)
ON CONFLICT DO NOTHING;
