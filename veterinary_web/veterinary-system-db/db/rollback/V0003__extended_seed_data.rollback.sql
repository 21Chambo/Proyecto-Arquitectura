-- Rollback de V0003__extended_seed_data.sql
-- Revierte los datos semilla agregados respetando dependencias.

DELETE FROM treatment
WHERE description IN (
    'Seed treatment plan 01',
    'Seed treatment plan 02',
    'Seed treatment plan 03',
    'Seed treatment plan 04',
    'Seed treatment plan 05',
    'Seed treatment plan 06',
    'Seed treatment plan 07',
    'Seed treatment plan 08',
    'Seed treatment plan 09',
    'Seed treatment plan 10'
);

DELETE FROM medical_record
WHERE diagnosis IN (
    'Seed diagnosis 01',
    'Seed diagnosis 02',
    'Seed diagnosis 03',
    'Seed diagnosis 04',
    'Seed diagnosis 05',
    'Seed diagnosis 06',
    'Seed diagnosis 07',
    'Seed diagnosis 08',
    'Seed diagnosis 09',
    'Seed diagnosis 10'
);

DELETE FROM appointment
WHERE reason IN (
    'Seed follow-up visit 01',
    'Seed follow-up visit 02',
    'Seed follow-up visit 03',
    'Seed follow-up visit 04',
    'Seed follow-up visit 05',
    'Seed follow-up visit 06',
    'Seed follow-up visit 07',
    'Seed follow-up visit 08',
    'Seed follow-up visit 09',
    'Seed follow-up visit 10'
);

DELETE FROM pet
WHERE name IN (
    'Seed Pet 01',
    'Seed Pet 02',
    'Seed Pet 03',
    'Seed Pet 04',
    'Seed Pet 05',
    'Seed Pet 06',
    'Seed Pet 07',
    'Seed Pet 08',
    'Seed Pet 09',
    'Seed Pet 10'
);

DELETE FROM veterinarian
WHERE document_number IN (
    'VET-2001',
    'VET-2002',
    'VET-2003',
    'VET-2004',
    'VET-2005',
    'VET-2006',
    'VET-2007',
    'VET-2008',
    'VET-2009',
    'VET-2010'
);

DELETE FROM customer
WHERE document_number IN (
    'CUST-1001',
    'CUST-1002',
    'CUST-1003',
    'CUST-1004',
    'CUST-1005',
    'CUST-1006',
    'CUST-1007',
    'CUST-1008',
    'CUST-1009',
    'CUST-1010'
);

DELETE FROM breed
WHERE name IN (
    'Seed Breed 01',
    'Seed Breed 02',
    'Seed Breed 03',
    'Seed Breed 04',
    'Seed Breed 05',
    'Seed Breed 06',
    'Seed Breed 07',
    'Seed Breed 08',
    'Seed Breed 09',
    'Seed Breed 10'
);

DELETE FROM species
WHERE name IN (
    'Seed Species 01',
    'Seed Species 02',
    'Seed Species 03',
    'Seed Species 04',
    'Seed Species 05',
    'Seed Species 06',
    'Seed Species 07',
    'Seed Species 08',
    'Seed Species 09',
    'Seed Species 10'
);
