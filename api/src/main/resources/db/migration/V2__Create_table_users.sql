CREATE TABLE users (
    id SERIAL,
    name TEXT NOT NULL,
    document TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role INTEGER,
    PRIMARY KEY (id),
    FOREIGN KEY (role) REFERENCES roles(id) ON DELETE CASCADE
);
