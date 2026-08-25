-- V3: Financial accounts belonging to a user
CREATE TABLE accounts (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    user_id    BIGINT         NOT NULL,
    name       VARCHAR(100)   NOT NULL,
    type       VARCHAR(20)    NOT NULL COMMENT 'CHECKING | SAVINGS | CREDIT | CASH',
    balance    DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency   VARCHAR(10)    NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
