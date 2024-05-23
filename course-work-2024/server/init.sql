\c postgres

DROP DATABASE IF EXISTS charging_stations_database;

CREATE DATABASE charging_stations_database
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8' --'Russian_Russia.1251'
    LC_CTYPE = 'en_US.UTF-8' --'Russian_Russia.1251'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE template0;

\c charging_stations_database

CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    address VARCHAR(256) NOT NULL
);

CREATE TABLE charging_stations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    address VARCHAR(256) NOT NULL,
    company_id INT NOT NULL,
    FOREIGN KEY (id) REFERENCES companies(id),
    opening_hours VARCHAR(5) NOT NULL,
    description VARCHAR(512)
);


INSERT INTO companies (name, address)
VALUES ('Завод-X', 'Moscow city'),
        ('Zavod-Y', 'город Москва');

INSERT INTO charging_stations (name, address, company_id, opening_hours, description)
VALUES ('BrusilovStation', 'город Москва, улица Брусилова', 1, '8-22', 'The best station'),
        ('RokossovskyStation', 'Moscow city, улица', 1, '0-24', NULL);

