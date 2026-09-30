# veterinary-system-db
Responsabilidad aislada de la base de datos del Sistema Veterinario.

## Contenido
- `docker-compose.yml`: levanta MySQL para el proyecto.
- `db/migration/`: migraciones versionadas para Flyway.
- `db/rollback/`: rollback manual por version.
- `db/CHANGELOG_DB.md`: historial de cambios SQL.
- `db/MIGRATIONS_GUIDE.md`: guia operativa de migraciones.
- `flyway.conf.example`: configuracion base de Flyway sin credenciales reales.

## Uso
```bash
docker network create veterinary-system-network
docker compose up -d
```

`db_unab` se crea desde la configuracion de MySQL usando `MYSQL_DATABASE` del archivo `.env`. Las migraciones Flyway no crean la base; crean y versionan el esquema dentro de `db_unab`.
MySQL se publica en `localhost:13306` por defecto; se puede cambiar con `MYSQL_PORT` si ese puerto tambien esta ocupado.

## Comandos basicos
```bash
docker compose run --rm flyway migrate 
docker compose run --rm flyway info
docker compose run --rm flyway validate
```

Para rollback manual, revisar la carpeta `db/rollback/` y ejecutar el script correspondiente de forma controlada.
