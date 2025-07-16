CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(100) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    category VARCHAR(10) NOT NULL,
    date DATE NOT NULL
);
