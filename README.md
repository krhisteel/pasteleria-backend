# Pastelería 1000 Sabores — Backend EV3

Spring Boot 3.5.7 con Java 17, JPA, Spring Security, JWT y Swagger. Conserva usuarios, categorías, productos, compras, boletas y los permisos existentes.

## Requisitos

- JDK 17 y Maven Wrapper incluido.
- Una base MariaDB creada y, para la primera preparación, completamente vacía.
- Un usuario de base de datos con acceso únicamente a esa base. Durante la preparación necesita crear tablas e índices y relacionarlas (`CREATE`, `ALTER`, `INDEX`, `REFERENCES`), además de leer e insertar las cuentas demo. Para la ejecución normal, necesita `SELECT`, `INSERT`, `UPDATE` y `DELETE`.
- React con su origen permitido en CORS.

No se necesita utilizar los puertos ni las credenciales del entorno de pruebas anterior. El host, puerto y nombre de base se eligen mediante variables. Este proyecto no crea la base automáticamente.

## 1. Configurar la terminal de ejecución

Ejemplo para PowerShell. Sustituir los datos de conexión por los propios; la base del ejemplo no se crea al ejecutar estos comandos.

```powershell
$env:JAVA_HOME = Read-Host 'Ruta de la instalación JDK 17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
.\mvnw.cmd -version

$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/pasteleria_ev3?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago'
$env:DB_USER = Read-Host 'Usuario de MariaDB limitado a esa base'
$claveDb = Read-Host 'Contraseña del usuario de MariaDB' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $claveDb).Password

$env:SERVER_PORT = '8080'
$env:APP_CORS_ORIGINS = 'http://localhost:5173'
```

El controlador incluido es MySQL Connector/J, por lo que la URL usa `jdbc:mysql://` también para MariaDB. En servidores remotos, configurar TLS según el servidor; los parámetros del ejemplo son para ejecución local. Si se cambia el puerto del backend o frontend, ajustar también `VITE_API_URL` y `APP_CORS_ORIGINS`. `localhost` y `127.0.0.1` son orígenes distintos para CORS.

Variables obligatorias: `DB_URL`, `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`. Spring Boot también acepta sus variables estándar `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`; utilizar una sola forma de configuración para evitar valores contradictorios.

## 2. Configurar JWT sin una clave pública de respaldo

Generar una clave privada para esta instalación y conservarla fuera de Git. No ejecutar de nuevo esta generación en cada reinicio: para conservar las sesiones, reutilizar la misma clave privada.

```powershell
$bytesJwt = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytesJwt)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytesJwt)
```

La variable contiene Base64 de bytes aleatorios; el backend requiere al menos 32 bytes decodificados. El código no ofrece un secreto predeterminado. No imprimir ni guardar el valor en documentos, capturas o archivos versionados.

## 3. Configurar las tres cuentas demo

Las cuentas conservan sus correos y roles: `admin@duoc.cl` (ADMIN), `vendedor@duoc.cl` (VENDEDOR) y `cliente@gmail.com` (CLIENTE). El login comprueba sus contraseñas BCrypt en MariaDB.

Antes de crear cuentas que todavía no existen, definir contraseñas privadas de **4 a 10 caracteres**, el rango que ya utiliza el formulario de login. Usar valores distintos y no compartirlos en Git.

```powershell
foreach ($rol in @('ADMIN', 'VENDEDOR', 'CLIENTE')) {
    $claveDemo = Read-Host "Contraseña privada para demo $rol (4 a 10 caracteres)" -AsSecureString
    [Environment]::SetEnvironmentVariable(
        "DEMO_${rol}_PASSWORD",
        [System.Net.NetworkCredential]::new('', $claveDemo).Password,
        'Process'
    )
}
```

`DataInitializer` solo crea las cuentas faltantes. Si una de ellas no tiene contraseña configurada válida, falla antes de insertar ninguna cuenta pendiente y solo informa el nombre de la variable. Si las cuentas ya existen, no exige estas variables, no cambia contraseñas ni modifica roles. Cambiar una variable no restablece una contraseña existente.

## 4. Preparar el esquema inicial, solo una vez en una base vacía

Confirmar el destino con un cliente SQL y comprobar que `SHOW TABLES` no devuelve tablas. No ejecutar esta preparación sobre una base existente.

```sql
SELECT DATABASE(), CURRENT_USER(), @@hostname;
SHOW TABLES;
```

Desde el repositorio, con JDK 17 y las variables anteriores disponibles:

```powershell
.\mvnw.cmd -DskipTests clean package
$env:JPA_DDL_AUTO = 'create-only'
java -jar target/pasteleria-0.0.1-SNAPSHOT.jar --spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true
```

El arranque crea `usuario`, `categoria`, `producto`, `boleta` y `detalle_boleta`; después crea las tres cuentas demo que falten. Comprobar el inicio correcto y los datos. Detener únicamente ese proceso con `Ctrl+C`. Las categorías y productos de demostración se agregan desde la aplicación; no se copian ni reinicializan bases existentes.

## 5. Ejecución normal con validación

En la misma terminal y manteniendo las credenciales y la clave JWT de la instalación:

```powershell
$env:JPA_DDL_AUTO = 'validate'
java -jar target/pasteleria-0.0.1-SNAPSHOT.jar
```

`validate` es el valor predeterminado. Si el esquema falta o es incompatible, el arranque falla en lugar de modificarlo. No usar `update` para resolver ese fallo automáticamente. No se ejecutan scripts SQL de inicialización.

Con el puerto del ejemplo:

- API: `http://localhost:8080/api/v1`.
- Swagger UI: `http://localhost:8080/swagger-ui.html`.
- OpenAPI: `http://localhost:8080/v3/api-docs`.

## Permisos y comprobaciones

ADMIN conserva las funciones administrativas. VENDEDOR solo consulta productos y órdenes. CLIENTE compra y consulta sus propias boletas. POST `/api/v1/boletas` admite únicamente ADMIN y CLIENTE. La configuración no cambia cálculos de compra, CRUD, catálogo ni contratos HTTP.

Pruebas seleccionadas sin base de datos (mocks y configuración en memoria):

```powershell
.\mvnw.cmd '-Dtest=DataInitializerTest,ConfiguracionSeguraTest,RolesSecurityTest,BoletaServiceTest,UsuarioServiceTest,JwtServiceTest' test
```

No ejecutar el test de contexto completo contra una base existente sin revisar previamente su destino. Los valores que aparecen en fixtures de tests son sintéticos y no son las claves de las cuentas demo de una instalación.

## Compose existente (opcional)

`docker-compose.yml` conserva su alternativa MySQL 8.4 y el volumen existente; no es necesario usarlo para ejecutar el backend con una MariaDB externa. Exige `MYSQL_ROOT_PASSWORD`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`; las tres variables `DEMO_*_PASSWORD` se necesitan si faltan cuentas. No admite root sin contraseña y utiliza un usuario distinto de root para la aplicación.

El puerto de base se puede configurar con `DB_PORT` y el del backend con `BACKEND_PORT`. El esquema no se modifica por defecto (`validate`); `JPA_DDL_AUTO=create-only` se utiliza únicamente para una base vacía. No reutilizar ni recrear volúmenes existentes para preparar otra instalación. No ejecutar Compose con secretos dentro del archivo. Un `.env` local, si se usa para Compose, está ignorado por Git; Spring Boot ejecutado directamente utiliza las variables de su proceso, no carga ese archivo automáticamente.

## Documentación de entrega

Los documentos EV3 y capturas están en `docs` del repositorio frontend: https://github.com/jaelreyes/pasteleria-react. Los documentos y ZIP de entrega están actualizados con la configuración corregida y verificada. Las portadas incorporan los datos académicos oficiales: integrantes Jael Reyes; Aileen Oyaneder; Benjamin Riquelme Pozo; docente VICTOR HUGO GOMEZ ATABALES; sección 003D; asignatura Desarrollo Fullstack II; evaluación Evaluación Parcial 3 (EV3). No se ha realizado commit ni push.
