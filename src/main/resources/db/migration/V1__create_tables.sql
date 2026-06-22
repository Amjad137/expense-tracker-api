-- =============================================================================
-- V1: Create all tables for Expense Tracker API
-- =============================================================================

-- Enable pgcrypto for gen_random_uuid() (available in PostgreSQL 13+)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =============================================================================
-- USERS
-- =============================================================================
CREATE TABLE users (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    is_deleted      BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255)
);

CREATE INDEX idx_users_email ON users(email) WHERE is_deleted = FALSE;

-- =============================================================================
-- REFRESH TOKENS (multi-device sessions)
-- =============================================================================
CREATE TABLE refresh_tokens (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512)    NOT NULL UNIQUE,
    device_info VARCHAR(255),
    expires_at  TIMESTAMP       NOT NULL,
    revoked     BOOLEAN         NOT NULL DEFAULT FALSE,
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);

-- =============================================================================
-- CATEGORIES
-- =============================================================================
CREATE TABLE categories (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(255),
    icon        VARCHAR(50),
    color       VARCHAR(20),
    is_system   BOOLEAN         NOT NULL DEFAULT FALSE,
    user_id     UUID            REFERENCES users(id) ON DELETE CASCADE,
    is_deleted  BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE INDEX idx_categories_user_id  ON categories(user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_categories_system   ON categories(is_system) WHERE is_deleted = FALSE;

-- =============================================================================
-- EXPENSES
-- =============================================================================
CREATE TABLE expenses (
    id           UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id  UUID            REFERENCES categories(id) ON DELETE SET NULL,
    title        VARCHAR(255)    NOT NULL,
    amount       DECIMAL(15, 2)  NOT NULL CHECK (amount > 0),
    description  TEXT,
    expense_date DATE            NOT NULL,
    is_deleted   BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at   TIMESTAMP,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255)
);

CREATE INDEX idx_expenses_user_id      ON expenses(user_id)      WHERE is_deleted = FALSE;
CREATE INDEX idx_expenses_category_id  ON expenses(category_id)  WHERE is_deleted = FALSE;
CREATE INDEX idx_expenses_date         ON expenses(expense_date)  WHERE is_deleted = FALSE;

-- =============================================================================
-- INCOMES
-- =============================================================================
CREATE TABLE incomes (
    id           UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id  UUID            REFERENCES categories(id) ON DELETE SET NULL,
    title        VARCHAR(255)    NOT NULL,
    amount       DECIMAL(15, 2)  NOT NULL CHECK (amount > 0),
    description  TEXT,
    income_date  DATE            NOT NULL,
    is_deleted   BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at   TIMESTAMP,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255)
);

CREATE INDEX idx_incomes_user_id  ON incomes(user_id)     WHERE is_deleted = FALSE;
CREATE INDEX idx_incomes_date     ON incomes(income_date) WHERE is_deleted = FALSE;

-- =============================================================================
-- BUDGETS
-- =============================================================================
CREATE TABLE budgets (
    id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id   UUID            REFERENCES categories(id) ON DELETE CASCADE,
    budget_type   VARCHAR(20)     NOT NULL DEFAULT 'GLOBAL',
    limit_amount  DECIMAL(15, 2)  NOT NULL CHECK (limit_amount > 0),
    year          INT             NOT NULL,
    month         INT             NOT NULL CHECK (month >= 1 AND month <= 12),
    is_deleted    BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255)
);

-- Partial unique indices to handle NULL category_id correctly
CREATE UNIQUE INDEX idx_budgets_global_unique
    ON budgets(user_id, year, month)
    WHERE budget_type = 'GLOBAL' AND is_deleted = FALSE;

CREATE UNIQUE INDEX idx_budgets_category_unique
    ON budgets(user_id, category_id, year, month)
    WHERE budget_type = 'CATEGORY' AND is_deleted = FALSE;

CREATE INDEX idx_budgets_user_id ON budgets(user_id) WHERE is_deleted = FALSE;

-- =============================================================================
-- RECURRING EXPENSES
-- =============================================================================
CREATE TABLE recurring_expenses (
    id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id   UUID            REFERENCES categories(id) ON DELETE SET NULL,
    title         VARCHAR(255)    NOT NULL,
    amount        DECIMAL(15, 2)  NOT NULL CHECK (amount > 0),
    description   TEXT,
    frequency     VARCHAR(20)     NOT NULL,
    start_date    DATE            NOT NULL,
    next_due_date DATE,
    end_date      DATE,
    is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
    is_deleted    BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP,
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255)
);

CREATE INDEX idx_recurring_expenses_user_id   ON recurring_expenses(user_id)       WHERE is_deleted = FALSE;
CREATE INDEX idx_recurring_expenses_due_date  ON recurring_expenses(next_due_date)  WHERE is_deleted = FALSE AND is_active = TRUE;
