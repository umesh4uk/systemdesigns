-- =============================================================
-- V8 : Seed data — admin user + sample category tree
-- =============================================================

-- Admin customer (password: Admin@123  →  BCrypt hash below)
INSERT INTO customers (id, email, password_hash, first_name, last_name, status)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin@ecommerce.example.com',
    '$2a$12$LwvYkHq5YE1q8ckgE4NaUeVwfJBimMNFSHFxXGYOJY0kQAbXwn9b2',  -- Admin@123
    'Platform',
    'Admin',
    'ACTIVE'
) ON CONFLICT DO NOTHING;

-- Root categories
INSERT INTO categories (id, name, slug, description, display_order)
VALUES
    ('c1000000-0000-0000-0000-000000000001', 'Electronics',  'electronics',  'Electronic devices and accessories', 1),
    ('c1000000-0000-0000-0000-000000000002', 'Clothing',      'clothing',     'Apparel and fashion',                2),
    ('c1000000-0000-0000-0000-000000000003', 'Home & Garden', 'home-garden',  'Home and garden products',           3),
    ('c1000000-0000-0000-0000-000000000004', 'Sports',        'sports',       'Sports and outdoor gear',            4)
ON CONFLICT DO NOTHING;

-- Electronics sub-categories
INSERT INTO categories (id, name, slug, description, parent_id, display_order)
VALUES
    ('c1100000-0000-0000-0000-000000000001', 'Mobiles',     'electronics-mobiles',     'Smartphones and accessories', 'c1000000-0000-0000-0000-000000000001', 1),
    ('c1100000-0000-0000-0000-000000000002', 'Laptops',     'electronics-laptops',     'Laptops and notebooks',       'c1000000-0000-0000-0000-000000000001', 2),
    ('c1100000-0000-0000-0000-000000000003', 'Accessories', 'electronics-accessories', 'Tech accessories',            'c1000000-0000-0000-0000-000000000001', 3)
ON CONFLICT DO NOTHING;

-- Clothing sub-categories
INSERT INTO categories (id, name, slug, description, parent_id, display_order)
VALUES
    ('c2100000-0000-0000-0000-000000000001', 'Men',   'clothing-men',   'Men''s clothing', 'c1000000-0000-0000-0000-000000000002', 1),
    ('c2100000-0000-0000-0000-000000000002', 'Women', 'clothing-women', 'Women''s clothing','c1000000-0000-0000-0000-000000000002', 2),
    ('c2100000-0000-0000-0000-000000000003', 'Kids',  'clothing-kids',  'Kids'' clothing',  'c1000000-0000-0000-0000-000000000002', 3)
ON CONFLICT DO NOTHING;
