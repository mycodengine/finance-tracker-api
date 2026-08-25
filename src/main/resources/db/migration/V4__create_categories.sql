-- V4: Categories — user_id NULL means system-wide default
CREATE TABLE categories (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT               DEFAULT NULL COMMENT 'NULL = system category',
    name       VARCHAR(50) NOT NULL,
    color      VARCHAR(7)  NOT NULL DEFAULT '#6366F1' COMMENT 'Hex color code',
    icon       VARCHAR(50)          DEFAULT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Seed system-wide default categories
INSERT INTO categories (name, color, icon) VALUES
    ('Food & Dining',     '#EF4444', 'restaurant'),
    ('Transportation',    '#F97316', 'car'),
    ('Shopping',          '#8B5CF6', 'shopping-bag'),
    ('Entertainment',     '#EC4899', 'film'),
    ('Health & Fitness',  '#10B981', 'heart'),
    ('Bills & Utilities', '#3B82F6', 'zap'),
    ('Travel',            '#06B6D4', 'plane'),
    ('Education',         '#84CC16', 'book'),
    ('Personal Care',     '#F59E0B', 'user'),
    ('Salary',            '#22C55E', 'briefcase'),
    ('Freelance',         '#14B8A6', 'laptop'),
    ('Investment',        '#A855F7', 'trending-up'),
    ('Other',             '#6B7280', 'more-horizontal');
