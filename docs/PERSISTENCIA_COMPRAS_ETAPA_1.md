# Persistencia de compras — Etapa 1

## 1. Objetivo y estado

Preparar los modelos JPA y los scripts versionados de compras. Esta etapa no implementa compras, cancelaciones, repositorios de compras, controladores ni integración con React.

**Ninguna migración fue ejecutada. No se modificaron datos ni esquemas existentes.**

`Producto.java`, su DTO y el PUT administrativo permanecen sin cambios. La columna propuesta `producto.version` no ofrecerá control de concurrencia hasta incorporar `@Version`, comprobar la versión enviada por el formulario y coordinar las escrituras de stock en una etapa posterior.

## 2. Estructura revisada

Se consultó `SHOW CREATE TABLE` mediante una transacción `READ ONLY` sobre la copia `pasteleria_crud_test`, en MariaDB 10.4.32, puerto 3307. No se consultaron registros personales ni se inspeccionó nuevamente la base original.

La copia contiene `categoria`, `producto` y `usuario`, con motor InnoDB, codificación `utf8mb4` y colación `utf8mb4_general_ci`. La relación existente es `producto.categoria_id → categoria.id`.

V1 reproduce sus tipos, longitudes, nulabilidad, claves primarias, unicidad y relación. Los nombres generados de índices se sustituyen por nombres descriptivos; se omiten contadores `AUTO_INCREMENT` y datos. La equivalencia de V1 con cualquier otro destino deberá revisarse explícitamente antes de establecer una línea base.

Las diferencias de nombres observadas son las siguientes. No implican renombrar objetos de una base existente: al establecer baseline V1 se omite la ejecución de ese script.

| Objeto existente en la copia inspeccionada | Nombre en V1 |
|---|---|
| Único de `categoria.nombre`: `UK35t4wyxqrevf09uwx9e9p6o75` | `uk_categoria_nombre` |
| Único de `producto.codigo`: `UKkxdt4u9c4w6vveo7ylph4pd09` | `uk_producto_codigo` |
| Único de `usuario.correo`: `UK2mlfr087gb1ce55f2j87o74t` | `uk_usuario_correo` |
| Único de `usuario.run`: `UKboxe2nbk8bf7qn3s3ig45xb69` | `uk_usuario_run` |
| Índice de `producto.categoria_id`: `FKodqr7965ok9rwquj1utiamt0m` | `ix_producto_categoria` |
| Clave foránea de `producto.categoria_id`: `FKodqr7965ok9rwquj1utiamt0m` | `fk_producto_categoria` |

El ancho de visualización `bigint(20)`/`int(11)` se expresa como `BIGINT`/`INT`, sin cambiar su capacidad. V1 es una referencia funcional, no una copia literal del DDL exportado.

## 3. Scripts propuestos

Ubicación: `src/main/resources/db/migration/`.

| Versión | Archivo | Efecto previsto, todavía no ejecutado |
|---|---|---|
| V1 | `V1__esquema_actual.sql` | Crear las tres tablas actuales únicamente en una base vacía, sin datos demo. |
| V2 | `V2__version_producto.sql` | Agregar `producto.version BIGINT NOT NULL DEFAULT 0`; no cambia precios, stock ni códigos. |
| V3 | `V3__crear_boleta.sql` | Crear cabecera de compra, propietario, despacho histórico, importes, estado y versión. |
| V4 | `V4__crear_detalle_boleta.sql` | Crear líneas con referencias y copias históricas de los productos y sus precios. |
| V5 | `V5__crear_solicitud_compra.sql` | Crear claves de idempotencia por usuario, huella y enlace único a la boleta. |

Los scripts no contienen INSERT, UPDATE o DELETE de datos, limpieza, datos iniciales, selección de la base original ni `IF NOT EXISTS` que oculte estructuras incompatibles. V2 es DDL: aplicarlo sí modifica la estructura y asigna el valor inicial de la nueva columna. Su ejecución requiere autorización posterior.

Flyway también creará su tabla técnica `flyway_schema_history` al establecer la línea base o migrar; es adicional a las tres tablas de negocio nuevas.

## 4. Modelos y restricciones

### Boleta

- Relación obligatoria y diferida con `Usuario`, sin cascadas hacia el usuario.
- Datos históricos de destinatario, correo, teléfono, dirección, región, comuna e indicaciones.
- Estados JPA: `PENDIENTE`, `PROCESANDO`, `ENVIADO`, `COMPLETADO`, `CANCELADO`, almacenados como VARCHAR con CHECK de comparación `BINARY`. Solo se admite esa escritura exacta, independientemente de la colación de la tabla. Los futuros DTO deberán mantener los valores en minúsculas utilizados por React.
- Versión optimista propia de la boleta. No se modificó el versionado de producto.
- Pago exclusivamente simulado; métodos `webpay` y `transferencia` son etiquetas del simulador. Su CHECK también utiliza comparación `BINARY`, por lo que variantes como `WEBPAY` se rechazan.
- Fechas con precisión de microsegundos. El futuro servicio deberá fijar una política horaria común antes de persistir.
- Importes `DECIMAL(16,2)`, descuento `DECIMAL(5,2)`, subtotal/total no negativos y total igual a subtotal menos ahorro. `ahorro` admite diferencias negativas de redondeo: con precio base 1,50 sin descuento, la fórmula actual de React produce total 2 y ahorro -0,50. Prohibirlo cambiaría la regla aprobada. El futuro servicio calculará siempre estos valores; no se aceptarán importes arbitrarios del cliente.
- Indicadores de descuento/restitución de stock y fecha de cancelación. La restitución exige descuento previo y estado cancelado mediante CHECK.
- Índices por propietario/fecha/ID y estado/fecha/ID.
- Teléfono de hasta 30 caracteres para conservar el formato visual actual con prefijo y espacios; el futuro DTO validará la cantidad de dígitos. Dirección de hasta 500 caracteres porque React envía la dirección compuesta.

### DetalleBoleta

- Relaciones obligatorias con `Boleta` y `Producto`.
- Código, nombre, imagen, precio base, descuento y precio final históricos; las columnas históricas no se actualizan mediante JPA.
- Restricción única por boleta y producto; índice para consultas por producto.
- Cantidad entre 1 y 50. Los límites globales de 30 productos y 200 unidades necesitan el futuro servicio transaccional.
- CHECK de importe de línea y de redondeo del precio unitario con `ROUND(..., 0)` para valores positivos. El cálculo autoritativo Java todavía no está implementado.

### SolicitudCompra

- Propietario obligatorio, UUID canónico en minúsculas y huella SHA-256 hexadecimal de 64 caracteres, sin almacenar tokens o credenciales.
- Unicidad por usuario y clave, con colación binaria ASCII para la clave y la huella.
- Enlace opcional y único a la boleta mientras se construye la futura transacción. El servicio deberá vincularlo antes de confirmar una compra y garantizar que ambos propietarios coincidan; estas reglas entre registros no están implementadas en esta etapa.
- No contiene datos de tarjetas, pagos reales ni el cuerpo completo de la compra.

Las claves foráneas usan `ON DELETE RESTRICT` y `ON UPDATE RESTRICT`. No hay cascadas JPA REMOVE/ALL ni `orphanRemoval`. La colección de detalles solo admite cascada PERSIST. Ninguna asociación permite borrar automáticamente usuarios, productos o comprobantes.

Los setters y las restricciones de mapeo no sustituyen la autorización ni las reglas transaccionales de las siguientes etapas. La consulta propia, las transiciones, la devolución única, la idempotencia y la prevención de stock negativo todavía no están implementadas.

## 5. Flyway y aislamiento de la ejecución

El perfil Maven `migraciones` incorpora únicamente el plugin Flyway 11.7.2, versión administrada por Spring Boot 3.5.7, su módulo `flyway-mysql` y el controlador MariaDB administrado por el mismo parent.

- No se agrega Flyway al classpath de ejecución de Spring Boot.
- El perfil no está activo por defecto y no tiene ejecuciones vinculadas al ciclo Maven.
- `baselineOnMigrate=false`, `cleanDisabled=true` y validación de nombres/checksums habilitada.
- No hay URL, usuario ni contraseña de migración versionados.
- Activar el perfil al compilar o ejecutar tests no ejecuta migraciones por sí solo.

Se comprobó la carga del plugin con Temurin JDK 17 mediante `flyway:info -Dflyway.skip=true`: Flyway informó que omitía la ejecución. No se estableció conexión ni se ejecutó `info` sobre un servidor. El primer intento con `flyway:help` no era válido porque este plugin no ofrece ese objetivo; se corrigió la comprobación sin acceder a bases de datos.

La documentación oficial incluye MariaDB 10.4 entre las versiones admitidas para migraciones y describe Java 17 como plataforma de ejecución. Esto y la carga del plugin no sustituyen la prueba pendiente de los scripts contra una base MariaDB desechable.

- [Compatibilidad de bases de datos](https://documentation.red-gate.com/flyway/getting-started-with-flyway/system-requirements/supported-databases-and-versions).
- [Ejecución Java de Flyway](https://documentation.red-gate.com/flyway/reference/usage/api-java).
- [Módulo de MariaDB](https://documentation.red-gate.com/flyway/reference/database-driver-reference/mariadb).
- [Línea base explícita](https://documentation.red-gate.com/flyway/reference/commands/baseline).

## 6. Procedimiento posterior: requiere autorización

### Requisitos comunes

- Trabajar con Temurin JDK 17 sobre recursos nuevos y exclusivos, con nombre previamente comprobado como disponible. No reutilizar ni alterar `pasteleria_db`, `pasteleria_crud_test`, sus volúmenes ni sus servidores.
- Comprobar host, puerto, nombre, identidad del servidor y permisos mediante consultas de lectura. Verificar que la identidad de migración solo puede operar en el destino autorizado. La aplicación no necesita permisos DDL.
- Proporcionar `FLYWAY_URL`, `FLYWAY_USER` y `FLYWAY_PASSWORD` por un mecanismo temporal seguro, nunca como contraseñas en argumentos, logs, archivos versionados o ejemplos del informe. El destino no se toma de `application.properties`.
- Mantener `baselineOnMigrate=false`, `cleanDisabled=true` y `validateOnMigrate=true`. No utilizar `repair`, `clean`, `outOfOrder=true` ni desactivar validaciones para continuar ante errores.

### Escenario A: base completamente vacía y desechable

1. Confirmar que la nueva base está vacía y no tiene historial Flyway. No establecer baseline: hacerlo omitiría V1 y dejaría ausentes las tablas del catálogo.
2. Con autorización específica para DDL, ejecutar `mvn -Pmigraciones flyway:migrate` para aplicar V1–V5. La validación interna previa permanece habilitada mediante `validateOnMigrate=true`.
3. Después ejecutar `mvn -Pmigraciones flyway:validate` y revisar `flyway_schema_history`, columnas, índices, claves foráneas y CHECK. Comprobar expresamente que estados en minúsculas y métodos en mayúsculas se rechazan cuando se autoricen las pruebas de escritura.
4. Validar JPA contra el esquema migrado con Hibernate en `validate`, inicializador excluido explícitamente y sin creación automática de esquema.

### Escenario B: copia desechable de un esquema existente

1. Preparar otra copia independiente autorizada, no la copia CRUD actualmente utilizada. Registrar una comparación completa con V1: tablas, columnas, tipos, nulabilidad, valores por defecto, índices únicos, claves foráneas, motor, codificación y colación.
2. Revisar y aprobar las diferencias de nombres documentadas en la sección 2. No ignorar diferencias funcionales, tablas adicionales o un historial Flyway previo. Si existen, detenerse y diseñar otra estrategia.
3. Solo tras comprobar equivalencia y autorizar la escritura del historial, establecer baseline explícito en versión 1. La línea base no demuestra equivalencia ni ejecuta V1.
4. Ejecutar `mvn -Pmigraciones flyway:migrate` para aplicar V2–V5, manteniendo la validación interna habilitada.
5. Después ejecutar `mvn -Pmigraciones flyway:validate`, revisar el esquema y comparar los datos preexistentes. Validar JPA con el inicializador excluido.

No se exige un `flyway:validate` independiente antes de migrar: con la configuración predeterminada puede fallar por las migraciones pendientes. No confundir ese caso con un checksum incorrecto ni desactivar la validación interna de `migrate`.

En ambos escenarios, MariaDB puede confirmar implícitamente DDL. No asumir rollback completo de un script fallido y no repararlo automáticamente. Las pruebas reales de restricciones e índices requieren una autorización posterior para escribir exclusivamente datos simulados en el destino desechable.

No se incluyen comandos de baseline listos para ejecutar contra un destino conocido: la aprobación de equivalencia y del destino es obligatoria antes de crear el historial.

## 7. Arranque y compatibilidad durante esta etapa

Se encontró `spring.jpa.hibernate.ddl-auto=update` en la configuración versionada y se cambió únicamente a `validate`, según la condición de seguridad solicitada. El resto de la configuración permanece intacto.

**Las nuevas entidades hacen que el arranque contra el esquema antiguo falle hasta aplicar V2–V5 en el destino autorizado.** No iniciar esta revisión contra ninguno de los entornos existentes. Los procesos ya iniciados no se reiniciaron ni fueron modificados.

`DataInitializer` tiene una condición explícita de registro controlada por `app.data-initializer.disabled`:

| Valor | Registro en el contexto |
|---|---|
| Propiedad ausente | Se conserva el comportamiento normal: bean registrado. |
| `false` | Exclusión desactivada; bean registrado. |
| `true` | Inicializador excluido: el bean no se crea y no puede ejecutar su runner. |

El interruptor de exclusión está desactivado por defecto para mantener el comportamiento normal. Para futuras pruebas aisladas será obligatorio configurar temporalmente `app.data-initializer.disabled=true` (equivalente de entorno: `APP_DATA_INITIALIZER_DISABLED=true`) y comprobar la ausencia del bean antes de permitir un arranque. No se cambió la configuración de los procesos existentes ni las cuentas demo.

`DataInitializerConfigurationTest` usa `ApplicationContextRunner`, sin la aplicación principal, servidor, JPA ni DataSource. Verifica los casos ausente, false y true, y la exclusión incluso sin repositorio ni codificador. Los contextos con el bean registrado no ejecutan `CommandLineRunner`; se comprueba que no hay interacciones con sus dependencias simuladas. No se ejecutó ningún inicializador.

Deshabilitar SQL init por sí solo no excluye un `CommandLineRunner`.

## 8. Verificaciones sin base de datos

- Construcción y validación de metadatos Hibernate de las seis entidades en memoria, sin crear EntityManagerFactory.
- Un proveedor JDBC de prueba falla si se intenta obtener una conexión.
- Comprobación de relaciones, cascadas, índices, precisión y correspondencia de columnas JPA con los scripts.
- Bean Validation de datos de prueba válidos e inválidos.
- Comprobación de que Hibernate usa validate y Flyway permanece opt-in, sin ejecuciones automáticas.
- Comprobación estática de las comparaciones binarias y de los valores exactos aprobados de estado y método de pago.
- Exclusión efectiva de DataInitializer en un contexto de prueba y conservación de su registro normal, sin ejecutar el runner.
- Suite Maven existente con servicios/repositorios simulados y sin DataSource, migraciones o inicializadores.

Estas verificaciones no ejecutan ni certifican todavía el DDL de MariaDB. Quedan pendientes la aplicación de V1–V5 en una base vacía desechable y baseline V1 seguido de V2–V5 en una copia equivalente, la validación Hibernate contra esos esquemas y las pruebas de restricciones reales. Las pruebas de compra, cancelación y concurrencia corresponden a etapas posteriores.
