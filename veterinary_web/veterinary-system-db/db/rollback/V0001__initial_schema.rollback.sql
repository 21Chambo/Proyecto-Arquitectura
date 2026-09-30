-- Rollback de V0001__initial_schema.sql
-- Revierte unicamente los objetos creados por la migracion inicial.

DROP TABLE IF EXISTS treatment;
DROP TABLE IF EXISTS medical_record;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS veterinarian;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS breed;
DROP TABLE IF EXISTS species;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
