CREATE TABLE IF NOT EXISTS usuarios (
    id VARCHAR(64) PRIMARY KEY,
    usuario VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS queens (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    photo_uri TEXT,
    envy_level FLOAT DEFAULT 0,
    shades_count INT DEFAULT 0,
    last_shade_date VARCHAR(32),
    song_id BIGINT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS shade_entries (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    queen_id VARCHAR(64) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(80),
    intensity FLOAT DEFAULT 0,
    date TIMESTAMP NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    location_address VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES usuarios(id),
    FOREIGN KEY (queen_id) REFERENCES queens(id)
);

