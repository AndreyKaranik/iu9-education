\c postgres

DROP DATABASE IF EXISTS charging_stations_database;
CREATE DATABASE charging_stations_database WITH ENCODING 'UTF8';

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
VALUES ('Zavod-Z', 'улица Z'),
        ('Zavod-Y', 'город Москва');

