# Sistema Veterinario

Aplicacion web para gestionar clientes, mascotas, especies, razas, veterinarios, citas, historias clinicas y tratamientos. El proyecto se compone de un frontend Angular, una API Spring Boot y una base de datos MySQL; Docker Compose coordina los servicios y Flyway aplica las migraciones.

## Requisitos

- Docker Desktop con Docker Compose v2.
- Puertos disponibles: `3000` (portal), `9090` (API) y `13306` (MySQL en el host).

## Inicio rapido en Windows

Desde esta carpeta (`veterinary_web`), crea la red compartida una sola vez:

```powershell
docker network create veterinary-system-network
```

Si Docker indica que la red ya existe, continua con el siguiente paso.

Opcionalmente, copia `.env.example` como `.env` para personalizar puertos y credenciales locales. Luego inicia el sistema:

```powershell
docker compose up --build -d --wait
```

Cuando los servicios esten listos, abre:

- Portal web: <http://localhost:3000>
- Swagger UI: <http://localhost:9090/swagger-ui.html>
- OpenAPI JSON: <http://localhost:9090/v3/api-docs>

El puerto `9090` corresponde a la API, no al portal. Abrir `http://localhost:9090/` puede mostrar un 404 porque la API no sirve una pagina web en la ruta raiz.

## Servicios y puertos

| Servicio | Contenedor | Puerto del host | Funcion |
| --- | --- | ---: | --- |
| Portal | `veterinary-portal` | `3000` | Aplicacion Angular servida por Nginx |
| API | `veterinary-api` | `9090` | API REST Spring Boot |
| MySQL | `veterinary-db` | `13306` | Base de datos; dentro de Docker escucha en `3306` |
| Flyway | `veterinary-flyway` | -- | Aplica migraciones y termina con codigo `0` |

La base persiste en el volumen Docker `veterinary-mysql-data`. Las migraciones versionadas estan en `veterinary-system-db/db/migration/`.

## Comandos utiles

Ver estado y salud:

```powershell
docker compose ps
```

Ver logs de un servicio:

```powershell
docker compose logs -f api
docker compose logs -f portal
docker compose logs -f db
```

Reconstruir y actualizar los servicios:

```powershell
docker compose up --build -d --wait
```

Detener los contenedores sin eliminar la base de datos:

```powershell
docker compose down
```

`docker compose down -v` tambien elimina el volumen de MySQL y sus datos. Usalo solo si realmente quieres borrar la base local.

## Configuracion local

Las variables estan documentadas en `.env.example`:

- `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER` y `MYSQL_PASSWORD` configuran MySQL y la conexion de la API.
- `MYSQL_PORT` cambia el puerto de MySQL publicado en el host; por defecto es `13306`.
- `API_PORT` cambia el puerto de la API; por defecto es `9090`.
- `PORTAL_PORT` cambia el puerto del portal; por defecto es `3000`.

Los valores predeterminados son solo para desarrollo local. Usa credenciales propias y no publiques archivos `.env` con secretos.

## Relaciones y eliminaciones

La base protege las relaciones entre clientes, mascotas, razas, citas, historias clinicas y tratamientos. Si un registro tiene elementos dependientes, la API responde `409 Conflict` con un mensaje que indica por que no se puede borrar. Elimina o reasigna primero los registros dependientes; el sistema no borra historiales relacionados en cascada.

## Estructura

```text
veterinary_web/
  docker-compose.yml
  veterinary-system-api/       API Spring Boot
  veterinary-system-db/        MySQL, migraciones y rollbacks
  veterinary-system-portal/    Frontend Angular y configuracion Nginx
```
