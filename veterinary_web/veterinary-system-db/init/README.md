Los scripts SQL originales de inicializacion fueron reorganizados para Flyway.

Trazabilidad:
- `init/01-schema.sql` -> `db/migration/V0001__initial_schema.sql`
- `init/02-auth-seed.sql` -> `db/migration/V0002__auth_seed.sql`

Esta carpeta se conserva solo como referencia historica.
