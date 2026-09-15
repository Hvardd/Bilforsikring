-- Existing demo agreements predate product selection; classify them as ANSVAR.
ALTER TABLE insurance_agreement
    ADD COLUMN insurance_type TEXT NOT NULL DEFAULT 'ANSVAR'
    CHECK (insurance_type IN ('ANSVAR', 'DELKASKO', 'KASKO', 'TOPPKASKO'));

-- New agreements must supply their chosen type explicitly.
ALTER TABLE insurance_agreement ALTER COLUMN insurance_type DROP DEFAULT;
