CREATE TABLE if not exists users (
user_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
firebase_uid VARCHAR(255) UNIQUE,
name VARCHAR(255) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE
);


INSERT INTO users (name, email, firebase_uid)
SELECT 'testuser4', 'testing4@test.no', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'testing4@test.no'
);

INSERT INTO users (name, email, firebase_uid)
SELECT 'anni', 'annitesting@gmail.com', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'annitesting@gmail.com'
);


INSERT INTO users (name, email, firebase_uid)
SELECT 'admin', 'Admin@admin.no', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'Admin@admin.no'
);

CREATE TABLE IF NOT EXISTS cabins (
cabin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
cabin_name VARCHAR(255) NOT NULL,
location VARCHAR(255),
capacity INT NOT NULL,
description TEXT
);

INSERT INTO cabins (cabin_name, location, capacity, description)
SELECT 'Fjellhytte', 'Geilo, Norge', 6, 'En koselig hytte med utsikt over fjellene, perfekt for en avslappende helg.'
WHERE NOT EXISTS (
    SELECT 1 FROM cabins WHERE cabin_name = 'Fjellhytte'
);

CREATE TABLE IF NOT EXISTS bookings (
booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT NOT NULL,
cabin_id BIGINT NOT NULL,
start_date DATE NOT NULL,
end_date DATE NOT NULL,
status VARCHAR(50) NOT NULL DEFAULT 'pending',
queue_position INT DEFAULT NULL,
price DECIMAL(10,2) NOT NULL,
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
FOREIGN KEY (cabin_id) REFERENCES cabins(cabin_id) ON DELETE CASCADE
);
