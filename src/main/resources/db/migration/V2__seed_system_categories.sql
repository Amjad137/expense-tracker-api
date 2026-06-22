-- =============================================================================
-- V2: Seed system categories
-- =============================================================================
-- System categories are shared across all users (user_id IS NULL, is_system = TRUE)
-- =============================================================================

INSERT INTO categories (name, description, icon, color, is_system, is_deleted, created_at, updated_at)
VALUES
  ('Food & Dining',      'Restaurants, groceries, and food delivery',              'utensils',        '#FF6B6B', TRUE, FALSE, NOW(), NOW()),
  ('Transportation',     'Fuel, public transit, rideshare, and vehicle maintenance','car',             '#4ECDC4', TRUE, FALSE, NOW(), NOW()),
  ('Housing & Rent',     'Rent, mortgage, and home maintenance',                   'home',            '#45B7D1', TRUE, FALSE, NOW(), NOW()),
  ('Utilities',          'Electricity, water, gas, and internet bills',            'zap',             '#96CEB4', TRUE, FALSE, NOW(), NOW()),
  ('Healthcare',         'Medical bills, prescriptions, and health insurance',     'heart',           '#FF8B94', TRUE, FALSE, NOW(), NOW()),
  ('Entertainment',      'Movies, concerts, streaming services, and hobbies',      'music',           '#A29BFE', TRUE, FALSE, NOW(), NOW()),
  ('Shopping',           'Clothing, electronics, and general shopping',            'shopping-bag',    '#FD79A8', TRUE, FALSE, NOW(), NOW()),
  ('Education',          'Tuition, books, courses, and training',                  'book',            '#6C5CE7', TRUE, FALSE, NOW(), NOW()),
  ('Travel',             'Flights, hotels, and vacation expenses',                 'map-pin',         '#00B894', TRUE, FALSE, NOW(), NOW()),
  ('Personal Care',      'Haircuts, gym membership, and personal hygiene',         'user',            '#FDCB6E', TRUE, FALSE, NOW(), NOW()),
  ('Investments',        'Stocks, mutual funds, and savings',                      'trending-up',     '#00CEC9', TRUE, FALSE, NOW(), NOW()),
  ('Insurance',          'Life, vehicle, and property insurance',                  'shield',          '#74B9FF', TRUE, FALSE, NOW(), NOW()),
  ('Gifts & Donations',  'Presents, charity, and donations',                       'gift',            '#E17055', TRUE, FALSE, NOW(), NOW()),
  ('Business',           'Business expenses and professional services',            'briefcase',       '#55EFC4', TRUE, FALSE, NOW(), NOW()),
  ('Salary',             'Primary employment income',                              'dollar-sign',     '#00B894', TRUE, FALSE, NOW(), NOW()),
  ('Freelance',          'Freelance and contract work income',                     'code',            '#6C5CE7', TRUE, FALSE, NOW(), NOW()),
  ('Rental Income',      'Income from property rentals',                           'key',             '#74B9FF', TRUE, FALSE, NOW(), NOW()),
  ('Other',              'Miscellaneous expenses and income',                      'more-horizontal', '#B2BEC3', TRUE, FALSE, NOW(), NOW());
