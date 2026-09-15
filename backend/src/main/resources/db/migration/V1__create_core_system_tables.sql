CREATE TABLE customer (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_number TEXT NOT NULL UNIQUE,
    national_identity_number VARCHAR(11) NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE insurance_agreement (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    agreement_number TEXT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    registration_number TEXT NOT NULL,
    bonus INTEGER NOT NULL CHECK (bonus BETWEEN 0 AND 75),
    status TEXT NOT NULL CHECK (status IN ('CREATED', 'SENT')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX insurance_agreement_customer_id_idx ON insurance_agreement(customer_id);
