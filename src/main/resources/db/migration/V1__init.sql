CREATE TABLE IF NOT EXISTS products (
	id VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
	name VARCHAR(255) NOT NULL,
	description VARCHAR(1000),
	base_price DECIMAL(15,2) NOT NULL,
	category VARCHAR(100),
	inventory_count INTEGER NOT NULL DEFAULT 0,
	total_inventory INTEGER NOT NULL DEFAULT 1000,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_history (
    id VARCHAR(36) PRIMARY KEY NOT NULL UNIQUE,
    user_id VARCHAR(36),
    username VARCHAR(255),
    membership_tier VARCHAR(20),
    product_id VARCHAR(36),
    product_name VARCHAR(255),
    base_price DECIMAL(15,2),
    final_price DECIMAL(15,2),
    total_discount DECIMAL(15,2),
    total_adjustment DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS competitor_prices (
	id VARCHAR(36) PRIMARY KEY,
	product_id VARCHAR(36) NOT NULL,
	competitor_name VARCHAR(255) NOT NULL,
	price DECIMAL(15,2) NOT NULL,	
	last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (product_id) REFERENCES products(id)
);


CREATE TABLE IF NOT EXISTS pricing_rules (
	id VARCHAR(36) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	description VARCHAR(1000),	
	rule_type VARCHAR(50) NOT NULL,
	priority INTEGER NOT NULL DEFAULT 500,
	enabled BOOLEAN NOT NULL DEFAULT TRUE,
	configuration TEXT,		
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS festivals (
	id VARCHAR(36) PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	start_date DATE NOT NULL,
	end_date DATE NOT NULL,
	discount_percentage DECIMAL(5,2) NOT NULL,
	enabled BOOLEAN NOT NULL DEFAULT TRUE,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS users (
	id VARCHAR(36) PRIMARY KEY,
	username VARCHAR(255) NOT NULL UNIQUE,
	email VARCHAR(255),
	membership_tier VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_competitor_prices_product_id 
ON competitor_prices(product_id);

CREATE INDEX idx_festivals_dates 
ON festivals(start_date, end_date);







