-- =========================================
-- EXTENDED SEED DATA
-- 10 registros nuevos por entidad, respetando dependencias
-- =========================================

INSERT IGNORE INTO species (name) VALUES
    ('Seed Species 01'),
    ('Seed Species 02'),
    ('Seed Species 03'),
    ('Seed Species 04'),
    ('Seed Species 05'),
    ('Seed Species 06'),
    ('Seed Species 07'),
    ('Seed Species 08'),
    ('Seed Species 09'),
    ('Seed Species 10');

INSERT INTO breed (species_id, name)
SELECT s.id, x.name
FROM (
    SELECT 'Seed Species 01' AS species_name, 'Seed Breed 01' AS name
    UNION ALL SELECT 'Seed Species 02', 'Seed Breed 02'
    UNION ALL SELECT 'Seed Species 03', 'Seed Breed 03'
    UNION ALL SELECT 'Seed Species 04', 'Seed Breed 04'
    UNION ALL SELECT 'Seed Species 05', 'Seed Breed 05'
    UNION ALL SELECT 'Seed Species 06', 'Seed Breed 06'
    UNION ALL SELECT 'Seed Species 07', 'Seed Breed 07'
    UNION ALL SELECT 'Seed Species 08', 'Seed Breed 08'
    UNION ALL SELECT 'Seed Species 09', 'Seed Breed 09'
    UNION ALL SELECT 'Seed Species 10', 'Seed Breed 10'
) x
INNER JOIN species s ON s.name = x.species_name
WHERE NOT EXISTS (
    SELECT 1
    FROM breed b
    WHERE b.name = x.name
);

INSERT IGNORE INTO customer (full_name, document_number, phone, email, address, created_at) VALUES
    ('Seed Customer 01', 'CUST-1001', '3001000001', 'customer01@veterinary.local', 'Seed Address 01', NOW()),
    ('Seed Customer 02', 'CUST-1002', '3001000002', 'customer02@veterinary.local', 'Seed Address 02', NOW()),
    ('Seed Customer 03', 'CUST-1003', '3001000003', 'customer03@veterinary.local', 'Seed Address 03', NOW()),
    ('Seed Customer 04', 'CUST-1004', '3001000004', 'customer04@veterinary.local', 'Seed Address 04', NOW()),
    ('Seed Customer 05', 'CUST-1005', '3001000005', 'customer05@veterinary.local', 'Seed Address 05', NOW()),
    ('Seed Customer 06', 'CUST-1006', '3001000006', 'customer06@veterinary.local', 'Seed Address 06', NOW()),
    ('Seed Customer 07', 'CUST-1007', '3001000007', 'customer07@veterinary.local', 'Seed Address 07', NOW()),
    ('Seed Customer 08', 'CUST-1008', '3001000008', 'customer08@veterinary.local', 'Seed Address 08', NOW()),
    ('Seed Customer 09', 'CUST-1009', '3001000009', 'customer09@veterinary.local', 'Seed Address 09', NOW()),
    ('Seed Customer 10', 'CUST-1010', '3001000010', 'customer10@veterinary.local', 'Seed Address 10', NOW());

INSERT IGNORE INTO veterinarian (full_name, document_number, phone, email, specialty, created_at) VALUES
    ('Seed Veterinarian 01', 'VET-2001', '3102000001', 'vet01@veterinary.local', 'General Medicine 01', NOW()),
    ('Seed Veterinarian 02', 'VET-2002', '3102000002', 'vet02@veterinary.local', 'General Medicine 02', NOW()),
    ('Seed Veterinarian 03', 'VET-2003', '3102000003', 'vet03@veterinary.local', 'General Medicine 03', NOW()),
    ('Seed Veterinarian 04', 'VET-2004', '3102000004', 'vet04@veterinary.local', 'General Medicine 04', NOW()),
    ('Seed Veterinarian 05', 'VET-2005', '3102000005', 'vet05@veterinary.local', 'General Medicine 05', NOW()),
    ('Seed Veterinarian 06', 'VET-2006', '3102000006', 'vet06@veterinary.local', 'General Medicine 06', NOW()),
    ('Seed Veterinarian 07', 'VET-2007', '3102000007', 'vet07@veterinary.local', 'General Medicine 07', NOW()),
    ('Seed Veterinarian 08', 'VET-2008', '3102000008', 'vet08@veterinary.local', 'General Medicine 08', NOW()),
    ('Seed Veterinarian 09', 'VET-2009', '3102000009', 'vet09@veterinary.local', 'General Medicine 09', NOW()),
    ('Seed Veterinarian 10', 'VET-2010', '3102000010', 'vet10@veterinary.local', 'General Medicine 10', NOW());

INSERT INTO pet (customer_id, breed_id, name, gender, birth_date, color, created_at)
SELECT c.id, b.id, x.name, x.gender, x.birth_date, x.color, NOW()
FROM (
    SELECT 'CUST-1001' AS document_number, 'Seed Breed 01' AS breed_name, 'Seed Pet 01' AS name, 'FEMALE' AS gender, DATE('2021-01-10') AS birth_date, 'Brown' AS color
    UNION ALL SELECT 'CUST-1002', 'Seed Breed 02', 'Seed Pet 02', 'MALE', DATE('2020-02-11'), 'Black'
    UNION ALL SELECT 'CUST-1003', 'Seed Breed 03', 'Seed Pet 03', 'FEMALE', DATE('2019-03-12'), 'White'
    UNION ALL SELECT 'CUST-1004', 'Seed Breed 04', 'Seed Pet 04', 'MALE', DATE('2018-04-13'), 'Golden'
    UNION ALL SELECT 'CUST-1005', 'Seed Breed 05', 'Seed Pet 05', 'FEMALE', DATE('2022-05-14'), 'Gray'
    UNION ALL SELECT 'CUST-1006', 'Seed Breed 06', 'Seed Pet 06', 'MALE', DATE('2021-06-15'), 'Cream'
    UNION ALL SELECT 'CUST-1007', 'Seed Breed 07', 'Seed Pet 07', 'FEMALE', DATE('2020-07-16'), 'Spotted'
    UNION ALL SELECT 'CUST-1008', 'Seed Breed 08', 'Seed Pet 08', 'MALE', DATE('2019-08-17'), 'Caramel'
    UNION ALL SELECT 'CUST-1009', 'Seed Breed 09', 'Seed Pet 09', 'FEMALE', DATE('2018-09-18'), 'Silver'
    UNION ALL SELECT 'CUST-1010', 'Seed Breed 10', 'Seed Pet 10', 'MALE', DATE('2022-10-19'), 'Tan'
) x
INNER JOIN customer c ON c.document_number = x.document_number
INNER JOIN breed b ON b.name = x.breed_name
WHERE NOT EXISTS (
    SELECT 1
    FROM pet p
    WHERE p.name = x.name
);

INSERT INTO appointment (pet_id, veterinarian_id, appointment_datetime, reason, status, created_at)
SELECT p.id, v.id, x.appointment_datetime, x.reason, x.status, NOW()
FROM (
    SELECT 'Seed Pet 01' AS pet_name, 'VET-2001' AS vet_document, TIMESTAMP('2026-06-01 08:00:00') AS appointment_datetime, 'Seed follow-up visit 01' AS reason, 'SCHEDULED' AS status
    UNION ALL SELECT 'Seed Pet 02', 'VET-2002', TIMESTAMP('2026-06-02 09:00:00'), 'Seed follow-up visit 02', 'COMPLETED'
    UNION ALL SELECT 'Seed Pet 03', 'VET-2003', TIMESTAMP('2026-06-03 10:00:00'), 'Seed follow-up visit 03', 'SCHEDULED'
    UNION ALL SELECT 'Seed Pet 04', 'VET-2004', TIMESTAMP('2026-06-04 11:00:00'), 'Seed follow-up visit 04', 'COMPLETED'
    UNION ALL SELECT 'Seed Pet 05', 'VET-2005', TIMESTAMP('2026-06-05 12:00:00'), 'Seed follow-up visit 05', 'SCHEDULED'
    UNION ALL SELECT 'Seed Pet 06', 'VET-2006', TIMESTAMP('2026-06-06 13:00:00'), 'Seed follow-up visit 06', 'COMPLETED'
    UNION ALL SELECT 'Seed Pet 07', 'VET-2007', TIMESTAMP('2026-06-07 14:00:00'), 'Seed follow-up visit 07', 'SCHEDULED'
    UNION ALL SELECT 'Seed Pet 08', 'VET-2008', TIMESTAMP('2026-06-08 15:00:00'), 'Seed follow-up visit 08', 'COMPLETED'
    UNION ALL SELECT 'Seed Pet 09', 'VET-2009', TIMESTAMP('2026-06-09 16:00:00'), 'Seed follow-up visit 09', 'SCHEDULED'
    UNION ALL SELECT 'Seed Pet 10', 'VET-2010', TIMESTAMP('2026-06-10 17:00:00'), 'Seed follow-up visit 10', 'COMPLETED'
) x
INNER JOIN pet p ON p.name = x.pet_name
INNER JOIN veterinarian v ON v.document_number = x.vet_document
WHERE NOT EXISTS (
    SELECT 1
    FROM appointment a
    WHERE a.reason = x.reason
);

INSERT INTO medical_record (appointment_id, diagnosis, notes, weight, temperature, created_at)
SELECT a.id, x.diagnosis, x.notes, x.weight, x.temperature, NOW()
FROM (
    SELECT 'Seed follow-up visit 01' AS appointment_reason, 'Seed diagnosis 01' AS diagnosis, 'Seed clinical note 01' AS notes, 8.50 AS weight, 38.1 AS temperature
    UNION ALL SELECT 'Seed follow-up visit 02', 'Seed diagnosis 02', 'Seed clinical note 02', 12.20, 38.5
    UNION ALL SELECT 'Seed follow-up visit 03', 'Seed diagnosis 03', 'Seed clinical note 03', 4.90, 38.0
    UNION ALL SELECT 'Seed follow-up visit 04', 'Seed diagnosis 04', 'Seed clinical note 04', 19.30, 38.7
    UNION ALL SELECT 'Seed follow-up visit 05', 'Seed diagnosis 05', 'Seed clinical note 05', 6.80, 38.2
    UNION ALL SELECT 'Seed follow-up visit 06', 'Seed diagnosis 06', 'Seed clinical note 06', 9.70, 38.4
    UNION ALL SELECT 'Seed follow-up visit 07', 'Seed diagnosis 07', 'Seed clinical note 07', 14.10, 38.3
    UNION ALL SELECT 'Seed follow-up visit 08', 'Seed diagnosis 08', 'Seed clinical note 08', 5.60, 38.6
    UNION ALL SELECT 'Seed follow-up visit 09', 'Seed diagnosis 09', 'Seed clinical note 09', 11.40, 38.2
    UNION ALL SELECT 'Seed follow-up visit 10', 'Seed diagnosis 10', 'Seed clinical note 10', 7.30, 38.1
) x
INNER JOIN appointment a ON a.reason = x.appointment_reason
WHERE NOT EXISTS (
    SELECT 1
    FROM medical_record mr
    WHERE mr.diagnosis = x.diagnosis
);

INSERT INTO treatment (medical_record_id, description, medication, dosage, duration, created_at)
SELECT mr.id, x.description, x.medication, x.dosage, x.duration, NOW()
FROM (
    SELECT 'Seed diagnosis 01' AS diagnosis, 'Seed treatment plan 01' AS description, 'Medication 01' AS medication, '5 ml' AS dosage, '5 days' AS duration
    UNION ALL SELECT 'Seed diagnosis 02', 'Seed treatment plan 02', 'Medication 02', '1 tablet', '7 days'
    UNION ALL SELECT 'Seed diagnosis 03', 'Seed treatment plan 03', 'Medication 03', '2 ml', '3 days'
    UNION ALL SELECT 'Seed diagnosis 04', 'Seed treatment plan 04', 'Medication 04', '1 capsule', '10 days'
    UNION ALL SELECT 'Seed diagnosis 05', 'Seed treatment plan 05', 'Medication 05', '3 ml', '4 days'
    UNION ALL SELECT 'Seed diagnosis 06', 'Seed treatment plan 06', 'Medication 06', '2 tablets', '6 days'
    UNION ALL SELECT 'Seed diagnosis 07', 'Seed treatment plan 07', 'Medication 07', '4 ml', '8 days'
    UNION ALL SELECT 'Seed diagnosis 08', 'Seed treatment plan 08', 'Medication 08', '1 dose', '2 days'
    UNION ALL SELECT 'Seed diagnosis 09', 'Seed treatment plan 09', 'Medication 09', '6 ml', '9 days'
    UNION ALL SELECT 'Seed diagnosis 10', 'Seed treatment plan 10', 'Medication 10', '1 tablet', '5 days'
) x
INNER JOIN medical_record mr ON mr.diagnosis = x.diagnosis
WHERE NOT EXISTS (
    SELECT 1
    FROM treatment t
    WHERE t.description = x.description
);
