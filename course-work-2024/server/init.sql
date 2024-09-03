\c postgres

DROP DATABASE IF EXISTS charging_stations_database;

CREATE DATABASE charging_stations_database
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8' --'Russian_Russia.1251'
    LC_CTYPE = 'en_US.UTF-8' --'Russian_Russia.1251'
    --LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE template0;

\c charging_stations_database

CREATE TABLE charging_stations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    address VARCHAR(256) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    opening_hours VARCHAR(10) NOT NULL,
    description VARCHAR(512)
);

CREATE TABLE charging_station_images (
    id SERIAL PRIMARY KEY,
    charging_station_id INT NOT NULL,
    path VARCHAR(256) NOT NULL,
    FOREIGN KEY (charging_station_id) REFERENCES charging_stations(id)
);

CREATE TABLE charging_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    current_type VARCHAR(32) NOT NULL
);

CREATE TABLE connectors (
    id SERIAL PRIMARY KEY,
    charging_station_id INT NOT NULL,
    status INT NOT NULL,
    charging_type_id INT NOT NULL,
    rate REAL NOT NULL,
    FOREIGN KEY (charging_station_id) REFERENCES charging_stations (id),
    FOREIGN KEY (charging_type_id) REFERENCES charging_types (id)
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    token VARCHAR(256) NULL UNIQUE,
    token_expiration TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE charging_marks (
    id SERIAL PRIMARY KEY,
    charging_station_id INT NOT NULL,
    status INT NOT NULL,
    user_id INT NULL,
    charging_type_id INT NOT NULL,
    time TIMESTAMP NOT NULL,
    FOREIGN KEY (charging_station_id) REFERENCES charging_stations (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);


INSERT INTO charging_stations (name, address, latitude, longitude, opening_hours, description)
VALUES
    ('Станция 1', 'ул. Тверская, 7, Москва', 55.756, 37.618, '10-21', 'Описание для Станции 1'),
    ('Станция 2', 'ул. Арбат, 10, Москва', 55.752, 37.586, '10-21', 'Описание для Станции 2'),
    ('Станция 3', 'ул. Мясницкая, 15, Москва', 55.762, 37.634, '10-21', NULL),
    ('Станция 4', 'ул. Новый Арбат, 8, Москва', 55.751, 37.596, '10-21', 'Описание для Станции 4'),
    ('Станция 5', 'ул. Лубянка, 2, Москва', 55.758, 37.625, '10-21', 'Описание для Станции 5'),
    ('Станция 6', 'ул. Моховая, 12, Москва', 55.754, 37.617, '10-21', 'Описание для Станции 6'),
    ('Станция 7', 'ул. Большая Дмитровка, 4, Москва', 55.759, 37.615, '10-21', 'Описание для Станции 7'),
    ('Станция 8', 'ул. Никольская, 25, Москва', 55.756, 37.621, '10-21', 'Описание для Станции 8'),
    ('Станция 9', 'ул. Большая Лубянка, 10, Москва', 55.761, 37.628, '10-21', 'Описание для Станции 9'),
    ('Станция 10', 'ул. Пречистенка, 20, Москва', 55.746, 37.595, '10-21', 'Описание для Станции 10'),
    ('Станция 11', 'ул. Большая Полянка, 32, Москва', 55.735, 37.618, '10-21', 'Описание для Станции 11'),
    ('Станция 12', 'ул. Тверская, 9, Москва', 55.757, 37.619, '10-21', 'Описание для Станции 12'),
    ('Станция 13', 'ул. Арбат, 12, Москва', 55.751, 37.585, '10-21', 'Описание для Станции 13'),
    ('Станция 14', 'ул. Мясницкая, 17, Москва', 55.763, 37.635, '10-21', 'Описание для Станции 14'),
    ('Станция 15', 'ул. Новый Арбат, 10, Москва', 55.750, 37.595, '10-21', 'Описание для Станции 15'),
    ('Станция 16', 'ул. Лубянка, 4, Москва', 55.757, 37.624, '10-21', 'Описание для Станции 16'),
    ('Станция 17', 'ул. Моховая, 14, Москва', 55.753, 37.616, '10-21', 'Описание для Станции 17'),
    ('Станция 18', 'ул. Большая Дмитровка, 6, Москва', 55.758, 37.614, '10-21', 'Описание для Станции 18'),
    ('Станция 19', 'ул. Никольская, 27, Москва', 55.755, 37.620, '10-21', 'Описание для Станции 19'),
    ('Станция 20', 'ул. Большая Лубянка, 12, Москва', 55.760, 37.627, '10-21', 'Описание для Станции 20'),
    ('Станция 21', 'ул. Пречистенка, 22, Москва', 55.745, 37.594, '10-21', 'Описание для Станции 21'),
    ('Станция 22', 'ул. Большая Полянка, 34, Москва', 55.734, 37.617, '10-21', 'Описание для Станции 22'),
    ('Станция 23', 'ул. Тверская, 11, Москва', 55.756, 37.618, '10-21', 'Описание для Станции 23'),
    ('Станция 24', 'ул. Арбат, 14, Москва', 55.750, 37.584, '10-21', 'Описание для Станции 24'),
    ('Станция 25', 'ул. Мясницкая, 19, Москва', 55.762, 37.634, '10-21', 'Описание для Станции 25'),
    ('Станция 26', 'ул. Новый Арбат, 12, Москва', 55.749, 37.594, '10-21', 'Описание для Станции 26'),
    ('Станция 27', 'ул. Лубянка, 6, Москва', 55.756, 37.623, '10-21', 'Описание для Станции 27'),
    ('Станция 28', 'ул. Моховая, 16, Москва', 55.752, 37.615, '10-21', 'Описание для Станции 28'),
    ('Станция 29', 'ул. Большая Дмитровка, 8, Москва', 55.757, 37.613, '10-21', 'Описание для Станции 29'),
    ('Станция 30', 'ул. Никольская, 29, Москва', 55.754, 37.619, '10-21', 'Описание для Станции 30'),
    ('Станция 31', 'ул. Большая Лубянка, 14, Москва', 55.759, 37.626, '10-21', 'Описание для Станции 31'),
    ('Станция 32', 'ул. Пречистенка, 24, Москва', 55.744, 37.593, '10-21', 'Описание для Станции 32');


INSERT INTO charging_station_images (charging_station_id, path)
VALUES (1, '1_1.jpg'),
        (1, '1_2.jpg'),
        (2, '2_1.jpg');

INSERT INTO charging_types (name, current_type)
VALUES ('TYPE 2', 'AC'),
        ('GB/T', 'DC');

INSERT INTO connectors (charging_station_id, status, charging_type_id, rate)
VALUES (1, 0, 1, 22),
        (1, 1, 1, 15),
        (1, 2, 1, 22),
        (1, 2, 1, 22),
        (2, 1, 1, 22),
        (2, 2, 2, 30);

INSERT INTO users (name, email, password, is_active)
VALUES ('Dmitry', 'dmitry@gmail.com', '$2a$10$RmeP/zA/5x3YHcnC8sY8VO2FeAENCdC0HFGv4tXYiya6vHQV.PtMy', true),
        ('Andrey', 'andrey@yandex.ru', '$2a$10$/WGAGy4DORJNAqtJUB4Sme/lsT9PMdjShQBkO3RepYtazzO9MN93q', true);

INSERT INTO charging_marks (charging_station_id, status, user_id, charging_type_id, time)
VALUES (1, 1, 1, 1, CURRENT_TIMESTAMP),
        (1, 1, 2, 1, CURRENT_TIMESTAMP),
        (2, 1, NULL, 1, CURRENT_TIMESTAMP),
        (2, 0, 2, 2, CURRENT_TIMESTAMP);