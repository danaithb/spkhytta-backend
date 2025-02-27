create table if not exists users (
    user_id int auto_increment primary key,
    email varchar(255) unique not null,
    name varchar(255),
    firebase_uid VARCHAR(255) UNIQUE
);