CREATE DATABASE IF NOT EXISTS etf_portfolio;
USE etf_portfolio;

-- ==================================================
-- Users table
-- ==================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- ==================================================
-- ETF table  
-- ==================================================
CREATE TABLE etf (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticker VARCHAR(10) NOT NULL,
    description VARCHAR(255) NOT NULL,
    asset_class VARCHAR(100) NOT NULL,
    expense_ratio DECIMAL(6,4) NOT NULL,
    user_id BIGINT NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==================================================
-- Portfolio table
-- ==================================================
CREATE TABLE portfolio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==================================================
-- Portfolio_ETF junction table (many-to-many)
-- ==================================================
CREATE TABLE portfolio_etf (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    etf_id BIGINT NOT NULL,
    FOREIGN KEY (portfolio_id) REFERENCES portfolio(id) ON DELETE CASCADE,
    FOREIGN KEY (etf_id) REFERENCES etf(id) ON DELETE CASCADE,
    UNIQUE KEY unique_portfolio_etf (portfolio_id, etf_id)
);

-- ==================================================
-- Sample Data
-- ==================================================

-- Insert demo users (password is 'password123' encoded with BCrypt)
INSERT INTO users (username, password, role) VALUES 
('admin', '$2a$10$XMFWEx.jTTSgusFWmAsGxum25jhYbhVt6UZUpEd3xBPdbOEyZITqu', 'ADMIN'),
('john_doe', '$2a$10$XMFWEx.jTTSgusFWmAsGxum25jhYbhVt6UZUpEd3xBPdbOEyZITqu', 'USER'),
('jane_smith', '$2a$10$XMFWEx.jTTSgusFWmAsGxum25jhYbhVt6UZUpEd3xBPdbOEyZITqu', 'USER'),
('bob_wilson', '$2a$10$XMFWEx.jTTSgusFWmAsGxum25jhYbhVt6UZUpEd3xBPdbOEyZITqu', 'USER'),
('alice_johnson', '$2a$10$XMFWEx.jTTSgusFWmAsGxum25jhYbhVt6UZUpEd3xBPdbOEyZITqu', 'USER');

-- Insert sample ETFs
INSERT INTO etf (ticker, description, asset_class, expense_ratio, user_id, is_public) VALUES 
('SPY', 'SPDR S&P 500 ETF Trust', 'Large Cap Equity', 0.0945, 1, TRUE),
('VTI', 'Vanguard Total Stock Market ETF', 'Total Market', 0.0300, 1, TRUE),
('QQQ', 'Invesco QQQ Trust', 'Technology', 0.2000, 1, TRUE),
('VEA', 'Vanguard FTSE Developed Markets ETF', 'International Equity', 0.0500, 1, TRUE),
('BND', 'Vanguard Total Bond Market ETF', 'Fixed Income', 0.0300, 1, TRUE),
('VWO', 'Vanguard Emerging Markets Stock ETF', 'Emerging Markets', 0.1000, 2, TRUE),
('IWM', 'iShares Russell 2000 ETF', 'Small Cap Equity', 0.1900, 2, FALSE),
('GLD', 'SPDR Gold Shares', 'Commodities', 0.4000, 3, TRUE),
('REIT', 'iShares U.S. Real Estate ETF', 'Real Estate', 0.4200, 3, FALSE),
('TLT', 'iShares 20+ Year Treasury Bond ETF', 'Government Bonds', 0.1500, 4, TRUE);

-- Insert sample portfolios
INSERT INTO portfolio (name, user_id, is_public) VALUES 
('Conservative Growth', 1, TRUE),
('Aggressive Tech', 2, FALSE),
('Balanced Income', 3, TRUE),
('International Focus', 4, FALSE),
('Core Holdings', 1, TRUE);

-- Insert portfolio-ETF relationships
INSERT INTO portfolio_etf (portfolio_id, etf_id) VALUES 
-- Conservative Growth (Portfolio 1)
(1, 1), -- SPY
(1, 2), -- VTI  
(1, 5), -- BND

-- Aggressive Tech (Portfolio 2)
(2, 3), -- QQQ
(2, 1), -- SPY
(2, 7), -- IWM

-- Balanced Income (Portfolio 3)
(3, 1), -- SPY
(3, 5), -- BND
(3, 8), -- GLD
(3, 9), -- REIT

-- International Focus (Portfolio 4)
(4, 4), -- VEA
(4, 6), -- VWO
(4, 1), -- SPY

-- Core Holdings (Portfolio 5)
(5, 1), -- SPY
(5, 2), -- VTI
(5, 4), -- VEA
(5, 5); -- BND

SELECT * FROM users;
