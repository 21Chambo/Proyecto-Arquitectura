-- Rollback de V0002__auth_seed.sql
-- Este rollback requiere revision manual antes de ejecutarse.
-- Motivo: la migracion usa INSERT IGNORE y no permite distinguir con total seguridad
-- si los roles ya existian antes de la carga semilla.
-- Puede ejecutarse de forma segura la eliminacion de relaciones y usuarios sembrados:

DELETE FROM user_roles
WHERE user_id IN (
    SELECT id
    FROM users
    WHERE username IN ('admin', 'staff')
);

DELETE FROM users
WHERE username IN ('admin', 'staff');

-- Revision manual requerida para la eliminacion de roles:
-- DELETE FROM roles
-- WHERE name IN ('ROLE_ADMIN', 'ROLE_STAFF');
