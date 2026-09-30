# Guia de Migraciones

## Motor detectado
Este proyecto usa MySQL 8.0.

## Creacion de la base de datos
`db_unab` no se crea dentro de una migracion Flyway. En este proyecto la base se crea al levantar MySQL mediante `MYSQL_DATABASE=db_unab` en el archivo `.env`.

Flyway se conecta despues a `db_unab` para crear y versionar tablas, constraints y datos semilla dentro de esa base.

## Estructura
- `db/migration/`: migraciones ejecutables por Flyway.
- `db/rollback/`: rollback manual por cada migracion.
- `db/CHANGELOG_DB.md`: indice documental de cambios.

## Como crear una nueva migracion
1. Crear un archivo nuevo en `db/migration/`.
2. Usar la siguiente convencion: `V0003__descripcion.sql`.
3. Crear su rollback manual equivalente en `db/rollback/` con el nombre `V0003__descripcion.rollback.sql`.
4. Registrar ambos archivos en `db/CHANGELOG_DB.md`.

## Convencion de nombres
- Version incremental sin reutilizar: `V0001`, `V0002`, `V0003`.
- Descripcion en minuscula.
- Separar palabras con guion bajo.
- No usar espacios.

## Ejecutar migraciones
```bash
docker compose run --rm flyway migrate

docker compose run --rm flyway -baselineVersion=1 baseline

```

## Consultar estado
```bash
docker compose run --rm flyway info
```

## Validar migraciones
```bash
docker compose run --rm flyway validate
```

## Rollback manual
1. Identificar la version a revertir.
2. Revisar el archivo correspondiente en `db/rollback/`.
3. Ejecutarlo manualmente sobre la base de datos correcta.
4. Validar despues con `flyway info` y con una verificacion funcional.

## Que no se debe hacer
- No editar migraciones ya ejecutadas.
- No reutilizar versiones existentes.
- No guardar rollback dentro de `flyway.locations`.
- No usar migraciones para crear la base de datos del proyecto.
- No usar `DROP DATABASE`.
- No usar `TRUNCATE` sin aprobacion explicita.
- No agregar SQL fuera de `db/migration/` si forma parte del historial.

## Checklist antes de subir cambios
- La migracion usa una version nueva.
- El nombre cumple la convencion Flyway.
- Existe rollback manual equivalente o comentario de revision manual.
- Se actualizo `db/CHANGELOG_DB.md`.
- Se valido con `docker compose run --rm flyway validate`.
