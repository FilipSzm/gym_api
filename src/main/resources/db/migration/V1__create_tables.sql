create table if not exists Person(
    id SERIAL PRIMARY KEY,
    name VARCHAR(127) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    age int NOT NULL,
    height int NOT NULL
);

create table if not exists Pet(
    id SERIAL PRIMARY KEY,
    name VARCHAR(127) NOT NULL,
    species VARCHAR(64) NOT NULL,
    owner_id INTEGER REFERENCES Person(id)
)
