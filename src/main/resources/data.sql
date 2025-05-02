CREATE TABLE if not exists users (
user_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
firebase_uid VARCHAR(255) UNIQUE,
name VARCHAR(255) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE,
points INT NOT NULL DEFAULT 12,
quarantine_end_date DATE DEFAULT NULL
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
SELECT 'Bella Belliosa', 'bella@gmail.no', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'bella@gmail.no'
);

INSERT INTO users (name, email, firebase_uid)
SELECT 'Alfred Bolle', 'bolle@gmail.no', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'bolle@gmail.no'
);

INSERT INTO users (name, email, firebase_uid)
SELECT 'admin', 'admin@admin.no', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@admin.no'
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

/*CREATE TABLE IF NOT EXISTS bookings (
booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
cabin_id BIGINT NOT NULL,
start_date DATE NOT NULL,
end_date DATE NOT NULL,
status VARCHAR(50) NOT NULL DEFAULT 'pending',
queue_position INT DEFAULT NULL,
price DECIMAL(10,2) NOT NULL,
booking_created_date DATETIME,
rest_booking BOOLEAN NOT NULL DEFAULT FALSE,
booking_code VARCHAR(255) UNIQUE,
number_of_guests INT NOT NULL DEFAULT 1,
points_required INT NOT NULL DEFAULT 0,
points_deducted INT NOT NULL DEFAULT 0,
points_before INT NOT NULL DEFAULT 0,
points_after INT NOT NULL DEFAULT 0,
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
FOREIGN KEY (cabin_id) REFERENCES cabins(cabin_id) ON DELETE CASCADE
);*/


CREATE TABLE IF NOT EXISTS bookings (
booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
cabin_id BIGINT NOT NULL,
start_date DATE NOT NULL,
end_date DATE NOT NULL,
status VARCHAR(50) NOT NULL DEFAULT 'pending',
price DECIMAL(10,2) NOT NULL,
booking_created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
booking_code VARCHAR(255) UNIQUE,
number_of_guests INT NOT NULL DEFAULT 1,
points_required INT NOT NULL DEFAULT 0,
queue_position INT DEFAULT NULL,
trip_type ENUM('PRIVATE','BUSINESS') NOT NULL DEFAULT 'PRIVATE',
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
FOREIGN KEY (cabin_id) REFERENCES cabins(cabin_id) ON DELETE CASCADE
);

CREATE TABLE waitlist_entries (
booking_id BIGINT PRIMARY KEY,
position INT NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

CREATE TABLE IF NOT EXISTS points_transactions (
points_id BIGINT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
points_change INT NOT NULL,
type VARCHAR(50) NOT NULL,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS booking_logs (
booking_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
booking_id BIGINT NOT NULL,
action VARCHAR(100) NOT NULL,
performed_by VARCHAR(100),
timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);








