# Pastelería 1000 Sabores — Backend

API de nuestra tienda de pastelería, desarrollada para Desarrollo Fullstack II de DUOC.

Se conecta con MariaDB y entrega los datos que utiliza el frontend: usuarios, productos, categorías, compras y boletas.

## Tecnologías

- Java 17 y Spring Boot 3.5.7.
- Spring Data JPA.
- Spring Security y JWT.
- MariaDB, con MySQL Connector/J.
- Swagger para consultar y probar la API.
- Maven Wrapper.

## Funcionalidades

- Registro, login y renovación de JWT.
- CRUD de productos y categorías.
- Compras con pago simulado y generación de boletas.
- Consulta de órdenes e historial de compras.
- Acceso según el rol del usuario.

ADMIN tiene acceso a las funciones administrativas. VENDEDOR solo puede consultar productos y órdenes. CLIENTE puede comprar y revisar sus propias boletas.

## Requisitos

Necesitas JDK 17 y una base MariaDB creada.

Para preparar el esquema por primera vez, utiliza una base vacía y un usuario con permisos para crear las tablas. En la ejecución normal, el usuario de la aplicación necesita permisos de lectura y escritura sobre esa base.

## Configuración

Desde PowerShell, comprueba que Java y Maven utilicen JDK 17:

```powershell
java -version
.\mvnw.cmd -version
```

Configura la conexión y los puertos en esa misma terminal:

```powershell
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/pasteleria_ev3?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago'
$env:DB_USER = Read-Host 'Usuario de la base de datos'
$claveDb = Read-Host 'Contraseña de la base de datos' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $claveDb).Password

$env:SERVER_PORT = '8080'
$env:APP_CORS_ORIGINS = 'http://localhost:5173'
$env:JPA_DDL_AUTO = 'validate'
```

Cambia el host, puerto y nombre de la base según tu instalación. La aplicación no crea la base de datos.

La URL utiliza `jdbc:mysql://` porque el proyecto incluye MySQL Connector/J, también utilizado para conectarse a MariaDB. Los parámetros del ejemplo son para una conexión local; en un servidor remoto debes configurar TLS.

### Clave JWT

`JWT_SECRET` es obligatorio. Debe contener una clave privada en Base64 con al menos 32 bytes aleatorios.

Puedes generar una para tu instalación sin mostrarla en pantalla:

```powershell
$bytesJwt = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytesJwt)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytesJwt)
```

Guárdala fuera del repositorio y reutiliza la misma en los siguientes arranques para mantener la validez de las sesiones. No la incluyas en documentos ni capturas.

### Cuentas de demostración

Al iniciar, la aplicación crea estas cuentas si todavía no existen:

| Correo | Rol |
| --- | --- |
| admin@duoc.cl | ADMIN |
| vendedor@duoc.cl | VENDEDOR |
| cliente@gmail.com | CLIENTE |

Para crearlas, configura contraseñas privadas y distintas de 4 a 10 caracteres, que es el rango permitido por el formulario actual:

```powershell
foreach ($rol in @('ADMIN', 'VENDEDOR', 'CLIENTE')) {
    $claveDemo = Read-Host "Contraseña demo $rol (4 a 10 caracteres)" -AsSecureString
    [Environment]::SetEnvironmentVariable(
        "DEMO_${rol}_PASSWORD",
        [System.Net.NetworkCredential]::new('', $claveDemo).Password,
        'Process'
    )
}
```

Si las cuentas ya existen, conservan sus contraseñas y roles. Cambiar estas variables no restablece sus contraseñas.

## Preparar una base vacía

Este paso se realiza una sola vez. Comprueba primero el destino desde tu cliente SQL:

```sql
SELECT DATABASE(), CURRENT_USER(), @@hostname;
SHOW TABLES;
```

Continúa únicamente si seleccionaste la base correcta y no tiene tablas.

Compila e inicia el backend con creación inicial del esquema:

```powershell
.\mvnw.cmd -DskipTests clean package
$env:JPA_DDL_AUTO = 'create-only'
java -jar target/pasteleria-0.0.1-SNAPSHOT.jar --spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true
```

Se crean las tablas de usuarios, categorías, productos, boletas y detalles, además de las cuentas demo faltantes.

Cuando termine de iniciar, detén ese proceso con `Ctrl+C`. Los productos y categorías se pueden agregar después desde el panel administrativo.

No utilices este paso sobre una base que ya tenga datos.

## Ejecución normal

Con las variables anteriores configuradas:

```powershell
$env:JPA_DDL_AUTO = 'validate'
java -jar target/pasteleria-0.0.1-SNAPSHOT.jar
```

`validate` comprueba el esquema sin modificarlo. Si falta una tabla o hay una incompatibilidad, el backend informa el error al iniciar.

Con los puertos del ejemplo:

- API: http://localhost:8080/api/v1
- Swagger: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

En React, configura `VITE_API_URL=http://localhost:8080/api/v1`. Su dirección debe coincidir con el origen permitido en `APP_CORS_ORIGINS`.

Las variables se configuran en el proceso que ejecuta Java. Al iniciar directamente con `java -jar`, Spring Boot no carga automáticamente un archivo `.env`.

## Equipo

- Jael Reyes.
- Aileen Oyaneder.
- Benjamin Riquelme Pozo.

Desarrollo Fullstack II · Sección 003D  
Docente: VICTOR HUGO GOMEZ ATABALES  
Evaluación Parcial 3 (EV3)

## Documentación

- [Frontend](https://github.com/jaelreyes/pasteleria-react).
- [Documentación y evidencias EV3](https://github.com/jaelreyes/pasteleria-react/tree/main/docs).
