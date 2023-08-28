CREATE TABLE transactions (
    id SERIAL,
    user_id INTEGER,
    counterparty_user_id INTEGER,
    type TEXT, 
    value NUMERIC(12,2) NOT NULL,
    datetime TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (counterparty_user_id) REFERENCES users(id) ON DELETE CASCADE
);