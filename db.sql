CREATE DATABASE IF NOT EXISTS govsim;
USE govsim;

-- ========================================
-- 1. USERS
-- ========================================
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 2. CITY_STATE
-- ========================================
CREATE TABLE city_state (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT    NOT NULL,
    month        INT    NOT NULL DEFAULT 1,
    year         INT    NOT NULL DEFAULT 1,
    budget       DOUBLE NOT NULL DEFAULT 1000000,
    satisfaction DOUBLE NOT NULL DEFAULT 100,
    population   INT    NOT NULL DEFAULT 100000,
    saved_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========================================
-- 3. MINISTERS
-- ========================================
CREATE TABLE ministers (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    city_id  INT          NOT NULL,
    name     VARCHAR(100) NOT NULL,
    ministry VARCHAR(50)  NOT NULL,
    score    DOUBLE       NOT NULL DEFAULT 100,
    warnings INT          NOT NULL DEFAULT 0,
    status   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (city_id) REFERENCES city_state(id) ON DELETE CASCADE
);

-- ========================================
-- 4. EVENTS_LOG
-- ========================================
CREATE TABLE events_log (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    minister_id INT         NOT NULL,
    day         INT         NOT NULL,
    ministry    VARCHAR(50) NOT NULL,
    severity    ENUM('NORMAL', 'DANGEROUS') NOT NULL,
    resolved    BOOLEAN     NOT NULL DEFAULT FALSE,
    FOREIGN KEY (minister_id) REFERENCES ministers(id) ON DELETE CASCADE
);

-- ========================================
-- 5. DECISIONS
-- ========================================
CREATE TABLE decisions (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    event_id   INT          NOT NULL,
    decision   VARCHAR(100) NOT NULL,
    cost       DOUBLE       NOT NULL DEFAULT 0,
    outcome    VARCHAR(255) DEFAULT 'PENDING',
    sat_impact DOUBLE       DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events_log(id) ON DELETE CASCADE
);

-- ========================================
-- 6. REPORTS
-- ========================================
CREATE TABLE reports (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    city_id      INT         NOT NULL,
    ministry     VARCHAR(50) NOT NULL,
    type         ENUM('MONTHLY', 'ANNUAL') NOT NULL,
    month        INT         NOT NULL,
    total_events INT         NOT NULL DEFAULT 0,
    resolved     INT         NOT NULL DEFAULT 0,
    rating       DOUBLE      NOT NULL DEFAULT 100,
    budget_end   DOUBLE      NOT NULL DEFAULT 0,
    FOREIGN KEY (city_id) REFERENCES city_state(id) ON DELETE CASCADE
);
