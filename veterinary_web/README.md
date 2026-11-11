# Sistema Veterinario

Aplicacion web para gestionar clientes, mascotas, especies, razas, veterinarios, citas, historias clinicas y tratamientos. El proyecto se compone de un frontend Angular, una API Spring Boot y una base de datos MySQL; Docker Compose coordina los servicios y Flyway aplica las migraciones.

## Requisitos

- Docker Desktop con Docker Compose v2.
- Para trabajar fuera de Docker: Java 17, Node 24 (24.15 o superior dentro de la rama 24) y Python 3 para validar Compose.
- Puertos disponibles: `3000` (portal), `9090` (API) y `13306` (MySQL en el host).

## Inicio rapido en Windows

Desde esta carpeta (`veterinary_web`), copia la configuracion de ejemplo:

```powershell
Copy-Item .env.example .env
```

Edita `.env` y define valores propios para `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`,
`APP_INITIAL_ADMIN_PASSWORD` y `APP_INITIAL_STAFF_PASSWORD`. Las claves de acceso
deben ser distintas, tener al menos 12 caracteres y no superar 72 bytes UTF-8.
No uses contrasenas vacias. Compose crea automaticamente la red. Inicia el sistema:

Si una clave contiene `$`, `#` o espacios, escribela entre comillas simples en
`.env` para conservar su valor literal. No compartas ni subas ese archivo a Git.

```powershell
docker compose up --build -d --wait
```

Cuando los servicios esten listos, abre:

- Portal web: <http://localhost:3000>
- Swagger UI: <http://localhost:9090/swagger-ui.html>
- OpenAPI JSON: <http://localhost:9090/v3/api-docs>
- Health check: <http://localhost:9090/api/health>

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

Ejecutar las pruebas del API antes de construir o enviar cambios:

```powershell
cd veterinary-system-api
./mvnw -B verify
```

En Windows PowerShell tambien se puede usar `mvnw.cmd -B verify`.

En Linux y GitHub Actions el wrapper ya esta versionado con permiso `100755` y
finales de linea LF. No requiere ejecutar `chmod` manualmente.
El job de pruebas del API usa Java 17
y ejecuta toda la suite, incluidas las pruebas agregadas despues de la lista
original de 81 casos.

Desde `veterinary_web`, en otra terminal, ejecutar las pruebas del portal sin
interfaz grafica ni vigilancia de archivos:

```sh
cd veterinary-system-portal
npm ci
npm run test:ci
```

El script usa `ChromeHeadlessCI` y termina al finalizar las pruebas. Requiere Chrome
o Chromium instalado; define `CHROME_BIN` si no se detecta automaticamente.
La configuracion interactiva de `npm test` se conserva para desarrollo.

El workflow reutilizable `.github/workflows/tests.yml` ejecuta las pruebas del
API y portal. El workflow de Trivy y GHCR depende de que ambas suites terminen
correctamente: solo entonces construye, escanea y publica las imagenes.
Los Dockerfiles pueden mantener `-DskipTests` porque esa dependencia bloquea la
publicacion si falla cualquier prueba. Esto no realiza despliegues en AWS.

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

### Credenciales de acceso

| Usuario | Rol | Contrasena inicial |
| --- | --- | --- |
| `admin` | Administrador | El valor que definas en `APP_INITIAL_ADMIN_PASSWORD` |
| `staff` | Personal | El valor que definas en `APP_INITIAL_STAFF_PASSWORD` |

No hay una contrasena universal: `admin123` y `staff123` no son claves predeterminadas.
En una base nueva, Flyway crea las cuentas y el API sustituye los hashes originales
de V0002 por hashes BCrypt de las claves configuradas. No edites migraciones ya
aplicadas ni ejecutes SQL manual para preparar el acceso.

En una base existente solo se corrigen cuentas que todavia conservan exactamente
el hash original de V0002. Las contrasenas personalizadas, roles, estado habilitado
y demas datos se conservan, incluidos ajustes locales anteriores. Reiniciar o cambiar
las variables **no restablece** las claves de cuentas ya inicializadas. Si conservas
una base anterior, usa su clave actual para esas cuentas; estas variables no son un
mecanismo de recuperacion de contrasenas.

El API falla al iniciar si una cuenta pendiente de inicializar no tiene una clave
valida configurada. Una clave incorrecta o una cuenta deshabilitada devuelve HTTP
`401` con un mensaje generico, sin revelar si existe el usuario.

Para comprobar el login desde PowerShell sin escribir la clave en el historial:

```powershell
$usuario = Read-Host 'Usuario (admin o staff)'
$credencial = Get-Credential -UserName $usuario -Message 'Acceso al sistema veterinario'
$body = @{ username = $usuario; password = $credencial.GetNetworkCredential().Password } | ConvertTo-Json
$sesion = Invoke-RestMethod -Method Post -Uri http://localhost:3000/api/auth/login -ContentType application/json -Body $body
$sesion | Select-Object username, fullName, roles
Remove-Variable body, credencial, sesion
```

La salud responde `503` durante el arranque y solo pasa a `ok` despues de completar
la inicializacion de credenciales y comprobar MySQL.

Prueba automatica de salud, ambos roles, acceso autenticado y rechazo de claves
incorrectas (no imprime claves ni tokens):

```powershell
$admin = Get-Credential -UserName admin -Message 'Clave actual de admin'
$staff = Get-Credential -UserName staff -Message 'Clave actual de staff'
.\scripts\Test-Authentication.ps1 -AdminCredential $admin -StaffCredential $staff
```

Para otro puerto o un despliegue con HTTPS, agrega `-BaseUrl https://tu-dominio`.
Si Windows bloquea scripts, habilitalos solo en la terminal de prueba con
`Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`; no cambia la politica permanente.

Las variables estan documentadas en `.env.example`:

- `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER` y `MYSQL_PASSWORD` configuran MySQL y la conexion de la API.
- `MYSQL_PORT` cambia el puerto de MySQL publicado en el host; por defecto es `13306`.
- `API_PORT` cambia el puerto de la API; por defecto es `9090`.
- `PORTAL_PORT` cambia el puerto del portal; por defecto es `3000`.

Las contraseñas no tienen valores predeterminados. Usa credenciales propias y no publiques archivos `.env` con secretos; `.env` está excluido de Git.

## Despliegue en producción

El archivo `compose.prod.yml` usa imagenes publicadas en GHCR y no construye imagenes en la instancia. Copia `.env.prod.example` como `.env.prod` y define `TAG`, `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, `APP_INITIAL_ADMIN_PASSWORD` y `APP_INITIAL_STAFF_PASSWORD`. Las variables ausentes o vacias impiden validar o iniciar Compose. Usa claves unicas para produccion, distintas de las de desarrollo. `TAG` debe ser el SHA de un commit cuyas imagenes hayan sido publicadas y escaneadas. `MYSQL_DATABASE` y `MYSQL_USER` pueden conservar sus valores de ejemplo:

```sh
docker compose --env-file .env.prod -f compose.prod.yml config --quiet
docker compose --env-file .env.prod -f compose.prod.yml pull
docker compose --env-file .env.prod -f compose.prod.yml up -d --wait
```

En produccion solo se publica el portal en el puerto `80`; la API y MySQL quedan sin puertos publicados. La inicializacion de credenciales es automatica y sigue las mismas reglas descritas arriba. Conserva el volumen de MySQL: volver a descargar el repositorio no elimina ni restablece las credenciales de una base existente. Configura HTTPS en el balanceador o proxy de AWS antes de exponer el login: la autenticacion actual usa Basic Auth y requiere cifrado en transito.

El mapeo es `80:8080`: Nginx escucha en `8080` dentro del contenedor para mantener
el usuario sin privilegios. Publicar `80:80` impediria acceder al servidor actual.
Todos los servicios rotan logs a `10m`, con tres archivos. DB, API y portal usan
`restart: unless-stopped`; Flyway se ejecuta una vez y no se reinicia en bucle.

El proyecto de produccion se llama `veterinary-production` y usa su propio volumen
`veterinary-production_mysql_data`; no reutiliza `veterinary-mysql-data` de desarrollo.
Si ya existe una instalacion de produccion con el nombre anterior, respalda sus datos
y planifica su migracion antes de cambiar el volumen; no borres el volumen anterior.

En el primer despliegue, define y verifica las nuevas claves de **admin y staff**.
Aunque V0002 contiene hashes historicos, el API los sustituye antes de estar listo
y falla si falta una clave valida. No edites V0002: alteraria el checksum de bases
existentes. Las cuentas ya personalizadas conservan su clave actual.

Validacion automatica, sin cargar `.env`, iniciar contenedores ni mostrar secretos:

```sh
python scripts/test_production_compose.py
```

La prueba exige cada secreto y `TAG` por separado, tanto ausentes como vacios,
y comprueba imagenes GHCR sin `build`, puertos privados, logs y volumen aislado.
Tambien corre en GitHub antes de construir imagenes.
Despues de un futuro despliegue autorizado, desde otro equipo se debe comprobar
que `nc -z -w 3 <ip-ec2> 13306` y `nc -z -w 3 <ip-ec2> 9090` no conectan.
Esta comprobacion remota no se ejecuta como parte de las pruebas locales.

## Dependencias y seguridad (cambio 14)

La API ya usa Spring Boot 4.0.9, Java 17 y `com.mysql:mysql-connector-j`.
Hibernate detecta el dialecto con la conexion MySQL; no se fuerza `MySQL57Dialect`
ni se agrega el antiguo `MySQL8Dialect` de Hibernate 5 a esta version moderna.
El portal se actualiza de Angular 10 a Angular 22, con Node 24, RxJS 7 y Bootstrap 5.
La imagen de ejecucion usa Nginx sin privilegios 1.30.6 y actualiza los paquetes Alpine.
Se retiran AdminLTE, jQuery, DataTables, Protractor, TSLint y ngx-toastr.
Los avisos usan Angular con interpolacion de texto, cierre manual y expiracion;
no se renderiza HTML recibido desde la API. No hay exclusiones `.trivyignore`.

Desde la raiz del repositorio, revisar primero todas las severidades, incluidas
las dependencias de desarrollo, y despues aplicar la puerta de calidad:

```sh
trivy fs --scanners vuln --include-dev-deps veterinary_web
docker build -t veterinary-api:verification veterinary_web/veterinary-system-api
trivy image --severity CRITICAL --exit-code 1 veterinary-api:verification
```

El filtro `CRITICAL` no certifica ausencia de vulnerabilidades de menor severidad.
El escaneo del codigo en CI tambien incluye las dependencias de desarrollo.
La suite actual del API tiene 88 pruebas, mas que los 81 casos del enunciado original.

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
