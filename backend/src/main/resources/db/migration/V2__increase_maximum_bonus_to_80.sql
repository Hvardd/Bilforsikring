ALTER TABLE insurance_agreement
    DROP CONSTRAINT insurance_agreement_bonus_check;

ALTER TABLE insurance_agreement
    ADD CONSTRAINT insurance_agreement_bonus_check CHECK (bonus BETWEEN 0 AND 80);
