INSERT INTO users (name, email, firebase_uid)
SELECT 'testuser4', 'testing4@test.no', ''
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'testing4@test.no'
);

INSERT INTO users (name, email, firebase_uid)
SELECT 'anni', 'annitesting@gmail.com', ''
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'annitesting@gmail.com'
);

INSERT INTO users (name, email, firebase_uid)
SELECT 'admin', 'Admin@admin.no', ''
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'Admin@admin.no'
);
