\c postgres

DROP DATABASE IF EXISTS charging_stations_database;

CREATE DATABASE charging_stations_database
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE template0;

\c charging_stations_database

CREATE TABLE charging_stations (
    id INT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    address VARCHAR(256) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    opening_hours VARCHAR(10) NOT NULL,
    description VARCHAR(512) NULL
);

CREATE TABLE charging_station_images (
    id INT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
    charging_station_id INT NOT NULL,
    path VARCHAR(256) NOT NULL,
    FOREIGN KEY (charging_station_id) REFERENCES charging_stations (id) ON DELETE CASCADE
);

CREATE TABLE charging_types (
    id INT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    current_type VARCHAR(32) NOT NULL
);

CREATE TABLE connectors (
    id INT PRIMARY KEY,
    charging_station_id INT NOT NULL,
    status INT NOT NULL,
    charging_type_id INT NOT NULL,
    rate REAL NOT NULL,
    FOREIGN KEY (charging_station_id) REFERENCES charging_stations (id) ON DELETE CASCADE,
    FOREIGN KEY (charging_type_id) REFERENCES charging_types (id)
);

CREATE TABLE users (
    id INT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    token VARCHAR(256) NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE charging_marks (
    id INT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
    charging_station_id INT NOT NULL,
    status INT NOT NULL,
    user_id INT NULL,
    charging_type_id INT NOT NULL,
    time TIMESTAMP NOT NULL,
    FOREIGN KEY (charging_station_id) REFERENCES charging_stations (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE orders (
    id INT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
    connector_id INT NOT NULL,
    user_id INT NULL,
    amount REAL NOT NULL,
    status INT NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    FOREIGN KEY (connector_id) REFERENCES connectors (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION prevent_pk_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'PK PREVENT EXCEPTION %', TG_TABLE_NAME;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION prevent_fk_update()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'FK PREVENT EXCEPTION %', TG_TABLE_NAME;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER charging_stations_pk_update_trigger
BEFORE UPDATE ON charging_stations
FOR EACH ROW
WHEN (OLD.id IS DISTINCT FROM NEW.id)
EXECUTE FUNCTION prevent_pk_update();

CREATE TRIGGER connectors_pk_update_trigger
BEFORE UPDATE ON connectors
FOR EACH ROW
WHEN (OLD.id IS DISTINCT FROM NEW.id)
EXECUTE FUNCTION prevent_pk_update();

CREATE TRIGGER charging_station_images_pk_update_trigger
BEFORE UPDATE ON charging_station_images
FOR EACH ROW
WHEN (OLD.id IS DISTINCT FROM NEW.id)
EXECUTE FUNCTION prevent_pk_update();

CREATE TRIGGER charging_marks_pk_update_trigger
BEFORE UPDATE ON charging_marks
FOR EACH ROW
WHEN (OLD.id IS DISTINCT FROM NEW.id)
EXECUTE FUNCTION prevent_pk_update();

CREATE TRIGGER charging_types_pk_update_trigger
BEFORE UPDATE ON charging_types
FOR EACH ROW
WHEN (OLD.id IS DISTINCT FROM NEW.id)
EXECUTE FUNCTION prevent_pk_update();

CREATE TRIGGER orders_pk_update_trigger
BEFORE UPDATE ON orders
FOR EACH ROW
WHEN (OLD.id IS DISTINCT FROM NEW.id)
EXECUTE FUNCTION prevent_pk_update();

CREATE TRIGGER users_pk_update_trigger
BEFORE UPDATE ON users
FOR EACH ROW
WHEN (OLD.id IS DISTINCT FROM NEW.id)
EXECUTE FUNCTION prevent_pk_update();

CREATE TRIGGER connectors_fk_update_trigger
BEFORE UPDATE ON connectors
FOR EACH ROW
WHEN (OLD.charging_station_id IS DISTINCT FROM NEW.charging_station_id
    OR OLD.charging_type_id IS DISTINCT FROM NEW.charging_type_id)
EXECUTE FUNCTION prevent_fk_update();

CREATE TRIGGER charging_station_images_fk_update_trigger
BEFORE UPDATE ON charging_station_images
FOR EACH ROW
WHEN (OLD.charging_station_id IS DISTINCT FROM NEW.charging_station_id)
EXECUTE FUNCTION prevent_fk_update();

CREATE TRIGGER charging_marks_fk_update_trigger
BEFORE UPDATE ON charging_marks
FOR EACH ROW
WHEN (OLD.charging_station_id IS DISTINCT FROM NEW.charging_station_id
    OR OLD.charging_type_id IS DISTINCT FROM NEW.charging_type_id
    OR OLD.user_id IS DISTINCT FROM NEW.user_id)
EXECUTE FUNCTION prevent_fk_update();

CREATE TRIGGER orders_fk_update_trigger
BEFORE UPDATE ON orders
FOR EACH ROW
WHEN (OLD.connector_id IS DISTINCT FROM NEW.connector_id
    OR OLD.user_id IS DISTINCT FROM NEW.user_id)
EXECUTE FUNCTION prevent_fk_update();


INSERT INTO charging_stations (id, name, address, latitude, longitude, opening_hours, description)
VALUES
    (1, 'Станция 1', 'ул. Тверская, 7, Москва', 55.756, 37.618, '10-21', 'Описание для Станции 1'),
    (2, 'Станция 2', 'ул. Арбат, 10, Москва', 55.752, 37.586, '10-21', 'Описание для Станции 2'),
    (3, 'Станция 3', 'ул. Мясницкая, 15, Москва', 55.762, 37.634, '10-21', NULL),
    (4, 'Станция 4', 'ул. Новый Арбат, 8, Москва', 55.751, 37.596, '10-21', 'Описание для Станции 4'),
    (5, 'Станция 5', 'ул. Лубянка, 2, Москва', 55.758, 37.625, '10-21', 'Описание для Станции 5');

INSERT INTO charging_station_images (charging_station_id, path)
VALUES (1, '1_1.jpg'),
        (1, '1_2.jpg'),
        (2, '2_1.jpg');

INSERT INTO charging_types (name, current_type)
VALUES ('TYPE 1', 'AC'),
        ('GB/T', 'DC'),
        ('TYPE 2', 'DC');

INSERT INTO connectors (id, charging_station_id, status, charging_type_id, rate)
VALUES (1, 1, 0, 1, 22),
        (2, 1, 1, 1, 15),
        (3, 1, 1, 1, 22),
        (4, 1, 1, 1, 22),
        (5, 2, 1, 1, 22),
        (6, 2, 1, 2, 30);

INSERT INTO users (name, email, password, is_active)
VALUES ('Dmitry', 'dmitry@gmail.com', '$2a$10$RmeP/zA/5x3YHcnC8sY8VO2FeAENCdC0HFGv4tXYiya6vHQV.PtMy', true),
        ('Andrey', 'andrey@yandex.ru', '$2a$10$/WGAGy4DORJNAqtJUB4Sme/lsT9PMdjShQBkO3RepYtazzO9MN93q', true);

INSERT INTO charging_marks (charging_station_id, status, user_id, charging_type_id, time)
VALUES (1, 1, 1, 1, CURRENT_TIMESTAMP),
        (1, 1, 2, 1, CURRENT_TIMESTAMP),
        (2, 1, NULL, 1, CURRENT_TIMESTAMP),
        (2, 0, 2, 2, CURRENT_TIMESTAMP);