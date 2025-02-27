INSERT INTO users (email, firebase_uid, name)
SELECT 'testing4@test.no', '', 'hanna banana'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'testing4@test.no');
