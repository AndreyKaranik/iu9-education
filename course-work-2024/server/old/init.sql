\c postgres

--ALTER database charging_stations_database is_template=false;
--DROP database charging_stations_database;

DROP DATABASE IF EXISTS charging_stations_database;

CREATE DATABASE charging_stations_database
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8' --'Russian_Russia.1251' --'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8' --'Russian_Russia.1251' --'en_US.UTF-8'
    LOCALE_PROVIDER = 'libc'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE template0;

-- ALTER database charging_stations_database is_template=true;

\c charging_stations_database


CREATE TABLE companies (
    company_id SERIAL PRIMARY KEY,
    company_name VARCHAR(32) NOT NULL,
    company_address VARCHAR(256) NOT NULL
);

CREATE TABLE charging_stations (
    charging_station_id SERIAL PRIMARY KEY,
    charging_station_name VARCHAR(32) NOT NULL,
    charging_station_address VARCHAR(256) NOT NULL,
    company_id INT NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(company_id),
    charging_station_opening_hours VARCHAR(5) NOT NULL,
    charging_station_description VARCHAR(512) NULL
);

INSERT INTO companies (company_name, company_address)
VALUES ('Zavod-Z', 'улица Ленина'),
        ('Zavod-Y', 'улица Сталина');

INSERT INTO charging_stations (charging_station_name, charging_station_address, company_id, charging_station_opening_hours, charging_station_description)
VALUES ('BrusilovStation', 'город Москва, улица Брусилова', 1, '8-22', 'The best station'),
        ('RokossovskyStation', 'Moscow city, улица Рокоссовского', 1, '0-24', NULL);

