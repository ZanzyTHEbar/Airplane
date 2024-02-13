PRAGMA foreign_keys = off;
BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS `airports` (
    `id` INTEGER NOT NULL,
    `ident` TEXT NOT NULL,
    `type` TEXT NOT NULL,
    `name` TEXT NOT NULL,
    `latitudeDeg` REAL NOT NULL,
    `longitudeDeg` REAL NOT NULL,
    `elevationFt` REAL,
    `continent` TEXT NOT NULL,
    `isoCountry` TEXT NOT NULL,
    `isoRegion` TEXT NOT NULL,
    `municipality` TEXT,
    `scheduledService` TEXT NOT NULL,
    `gpsCode` TEXT,
    `iataCode` TEXT,
    `localCode` TEXT,
    `homeLink` TEXT,
    `wikipediaLink` TEXT,
    `keywords` TEXT,
    PRIMARY KEY(`id`)
);
CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT);
INSERT
    OR REPLACE INTO room_master_table (id, identity_hash)
VALUES(42, '87e0140b5fed0f4740ca11ff900d96a9');
/* ALTER TABLE airports 
 RENAME TO _airports_old;
 */
/* CREATE TABLE airports (
 id INTEGER NOT NULL PRIMARY KEY,
 ident TEXT,
 type TEXT NOT NULL,
 name TEXT NOT NULL,
 latitudeDeg REAL NOT NULL,
 longitudeDeg REAL NOT NULL,
 elevationFt REAL,
 continent TEXT NOT NULL,
 isoCountry TEXT NOT NULL,
 isoRegion TEXT NOT NULL,
 municipality TEXT,
 scheduledService TEXT NOT NULL,
 gpsCode TEXT,
 iataCode TEXT,
 localCode TEXT,
 homeLink TEXT,
 wikipediaLink TEXT,
 keywords TEXT
 ); */
/* INSERT INTO airports (
 id,
 ident,
 longitudeDeg,
 latitudeDeg,
 elevationFt,
 continent,
 isoRegion,
 isoCountry,
 gpsCode,
 iataCode,
 localCode,
 name,
 type,
 scheduledService,
 municipality,
 wikipediaLink,
 homeLink,
 keywords
 )
 SELECT id,
 ident,
 CAST(longitude_deg AS REAL),
 CAST(latitude_deg AS REAL),
 CAST(elevation_ft AS REAL),
 continent,
 iso_region,
 iso_country,
 gps_code,
 iata_code,
 local_code,
 name,
 type,
 scheduled_service,
 municipality,
 wikipedia_link,
 home_link,
 keywords
 FROM _airports_old; */
COMMIT;
PRAGMA foreign_keys = on;