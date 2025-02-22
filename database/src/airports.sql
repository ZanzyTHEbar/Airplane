CREATE TABLE airports (
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
);
