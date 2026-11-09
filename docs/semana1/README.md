# Semana 1 · Arquitectura web y Docker

El sistema veterinario ejecuta el portal Angular, la API Spring Boot y MySQL desde el Compose de `veterinary_web`. Flyway aplica el esquema versionado antes de que arranque la API.

## Flujo de arranque y de peticiones

1. MySQL escucha en `db:3306` dentro de Docker. Flyway espera a que MySQL responda y ejecuta las migraciones de [db/migration](../../veterinary_web/veterinary-system-db/db/migration).
2. Spring Boot espera a que Flyway termine correctamente. Hibernate valida el esquema y `GET /api/health` comprueba la conexión a MySQL. Devuelve HTTP 503 si falla.
3. Nginx espera a que la API esté lista, sirve Angular y reenvía `/api/*` a Spring Boot. El navegador llama al mismo origen del portal. Durante el desarrollo, Angular CLI reenvía esas rutas a la API local.

## Ejecutar en Windows

Desde la raíz del repositorio, entra en `veterinary_web`. Si necesitas cambiar puertos o credenciales locales, copia `.env.example` como `.env`.

```powershell
Set-Location veterinary_web
docker compose config --quiet
docker compose up --build -d --wait
docker compose ps -a
curl.exe -fsS http://localhost:9090/api/health
curl.exe -fsS http://localhost:3000/api/health
```

Abre el [portal local](http://localhost:3000) y recarga una ruta del portal para comprobar que Nginx entrega la aplicación Angular. [Swagger](http://localhost:9090/swagger-ui.html) permite inspeccionar los endpoints. Las dos comprobaciones de salud deben devolver `status: ok`. En `docker compose ps -a`, es normal que Flyway figure como terminado con código 0.

MySQL se publica en el puerto `13306` por defecto y guarda los datos en `veterinary-mysql-data`. `docker compose down` detiene el entorno y conserva ese volumen. En una base previa, Flyway valida el historial existente. Si detecta diferencias, consulta `docker compose logs flyway` antes de modificar el esquema o el volumen.

## Desarrollo y HTTPS local

Con la API disponible, ejecuta `npm ci` y `npm start` desde `veterinary-system-portal` y abre [Angular CLI](http://localhost:4200). El proxy usa el puerto `9090` por defecto. Si lo cambiaste en Compose, define el mismo `API_PORT` en la terminal de Angular.

Para probar HTTPS con un certificado autofirmado, sigue las instrucciones del [portal](../../veterinary_web/veterinary-system-portal/README.md#local-https-with-self-signed-certificate). El archivo adicional de Compose habilita TLS local en el puerto `3443` por defecto.

## Archivos generados

El `.gitignore` de `veterinary_web` excluye los nuevos logs y los artefactos de compilación. Los dos logs de dashboard que ya estaban versionados siguen disponibles para revisión. Se conserva el `.dockerignore` de la raíz y se excluyen los logs, los archivos de entorno y los certificados locales de los contextos que usan las compilaciones.
