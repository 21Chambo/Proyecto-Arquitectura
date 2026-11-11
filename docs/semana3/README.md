# Semana 3: credenciales iniciales y preparacion del despliegue

La rama `semana3` corrige el acceso de las cuentas iniciales sin depender de
ajustes manuales de una base local. No publica secretos ni realiza un despliegue
en AWS.

## Cambios

- El API inicializa las cuentas `admin` y `staff` usando
  `APP_INITIAL_ADMIN_PASSWORD` y `APP_INITIAL_STAFF_PASSWORD`. Solo reemplaza los
  hashes originales de la migracion V0002 y almacena las nuevas claves con BCrypt.
- Las claves iniciales requieren al menos 12 caracteres y como maximo 72 bytes
  UTF-8. No existen claves universales en el repositorio. Cada entorno define las
  suyas en variables de entorno o en su `.env` privado.
- Las cuentas ya personalizadas conservan sus claves, roles y estado habilitado.
  Cambiar las variables iniciales no restablece una clave existente.
- Las claves incorrectas, los usuarios inexistentes y las cuentas deshabilitadas
  reciben HTTP 401 con un mensaje generico. Los errores internos del servicio de
  autenticacion conservan HTTP 500; no se ocultan como errores de credenciales.
- `/api/health` solo responde `ok` despues de terminar la inicializacion y
  comprobar MySQL. Durante el arranque devuelve HTTP 503.
- El Compose de produccion recibe las variables iniciales, espera la salud del
  API y usa el puerto 8080 y el alias de red que requiere Nginx.
- No se modifican las migraciones ya aplicadas ni se borran volumenes existentes.

## Validacion realizada

El 11 de noviembre de 2026 se verificaron:

- `mvn -B verify` con Java 11 dentro de Docker: **88 pruebas**, sin fallos,
  errores ni pruebas omitidas.
- Inicializacion con una base MySQL nueva, login de ambos usuarios, sus roles y
  acceso autenticado al API a traves de Nginx.
- Rechazo de claves incorrectas y usuarios inexistentes con HTTP 401; datos de
  login incompletos con HTTP 400.
- Conservacion de las claves al reiniciar y al usar una base ya inicializada.
- Compose de produccion con imagenes construidas localmente y una base aislada.
  Esta comprobacion no equivale a publicar esas imagenes en GHCR ni a probar AWS.
- Conservacion de la clave de administrador y de los conteos de usuarios, roles,
  clientes, mascotas, citas, historias clinicas y tratamientos en la base local.

La clave de personal que se restablecio en la base local es una personalizacion
de ese entorno. Ese cambio de datos y el archivo `.env` no se incluyen en Git.
Un clon nuevo utiliza las claves que configure su operador.

## Probar un clon nuevo

```powershell
git clone --branch semana3 https://github.com/21Chambo/Proyecto-Arquitectura.git
Set-Location Proyecto-Arquitectura/veterinary_web
Copy-Item .env.example .env
```

Edita `.env` y completa `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`,
`APP_INITIAL_ADMIN_PASSWORD` y `APP_INITIAL_STAFF_PASSWORD` con valores propios.
Las claves de acceso deben cumplir las longitudes indicadas arriba. Conserva
ese archivo fuera de Git. Si reutilizas un volumen existente, las cuentas ya
personalizadas mantienen sus contrasenas anteriores.

```powershell
docker compose config --quiet
docker compose up --build -d --wait
docker compose ps -a
curl.exe -fsS http://localhost:3000/api/health
$admin = Get-Credential -UserName admin -Message 'Clave actual de admin'
$staff = Get-Credential -UserName staff -Message 'Clave actual de staff'
.\scripts\Test-Authentication.ps1 -AdminCredential $admin -StaffCredential $staff
```

Si Windows bloquea scripts, puedes habilitarlos solo en la terminal de prueba con
`Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`. No cambia la politica
permanente del equipo. Consulta el [README del sistema](../../veterinary_web/README.md)
para los detalles de configuracion y pruebas.

## Auditoria del portal

La consulta `npm audit --package-lock-only --json` del 11 de noviembre de 2026
reporto sobre el lockfile de esta rama:

| Severidad | Cantidad |
| --- | ---: |
| Baja | 12 |
| Moderada | 77 |
| Alta | 82 |
| Critica | 18 |
| Total | 189 |

El resultado incluye dependencias de desarrollo y transitivas. No significa que
todas esas incidencias esten presentes o sean explotables en la imagen Nginx
final. Tambien deben auditarse las imagenes que se vayan a publicar. No se ejecuto
`npm audit fix --force`: actualizar dependencias y la version antigua de Angular
requiere revisar compatibilidad, compilar y probar nuevamente el portal.

## Pendiente antes de AWS

1. Revisar las vulnerabilidades del portal y de las imagenes, aplicar las
   actualizaciones acordadas y repetir las pruebas correspondientes.
2. Publicar las imagenes corregidas del API y portal en GHCR con un tag asociado
   al commit aprobado. Subir esta rama por si solo no publica imagenes.
3. Configurar `TAG`, las claves de MySQL y las claves iniciales propias del entorno
   AWS. No copiar las credenciales locales ni publicar `.env` en el repositorio.
4. Configurar HTTPS en el balanceador o proxy y el dominio correspondiente. El
   login actual usa Basic Auth y requiere cifrado en transito.
5. Levantar `compose.prod.yml` con las imagenes nuevas y verificar salud, login de
   ambos roles y acceso al API mediante el dominio HTTPS. Conservar el volumen de
   MySQL y disponer de una copia de seguridad antes de actualizar datos existentes.

La correccion de credenciales esta validada en Docker. El despliegue en AWS y la
revision completa de seguridad siguen pendientes.
