-- V5: Transactions linked to accounts and categories
CREATE TABLE transactions (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    account_id       BIGINT         NOT NULL,
    category_id      BIGINT                  DEFAULT NULL,
    type             VARCHAR(20)    NOT NULL COMMENT 'INCOME | EXPENSE | TRANSFER',
    amount           DECIMAL(15, 2) NOT NULL,
    description      VARCHAR(255)            DEFAULT NULL,
    transaction_date DATE           NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_transactions_account  FOREIGN KEY (account_id)  REFERENCES accounts (id)   ON DELETE CASCADE,
    CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,

    -- Speeds up date-range queries per account
    INDEX idx_transactions_account_date (account_id, transaction_date),
    INDEX idx_transactions_type         (type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
