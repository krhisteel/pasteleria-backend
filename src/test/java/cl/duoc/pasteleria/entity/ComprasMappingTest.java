package cl.duoc.pasteleria.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import java.sql.Connection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Construye y valida metadatos Hibernate en memoria. No crea EntityManagerFactory.
class ComprasMappingTest {
    private static StandardServiceRegistry registry;
    private static MetadataImplementor metadata;

    @BeforeAll
    static void prepararMetadatos() {
        registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect")
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .applySetting("hibernate.hbm2ddl.auto", "none")
                .applySetting("hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .applySetting("hibernate.connection.provider_class", SinConexion.class.getName())
                .build();
        metadata = (MetadataImplementor) new MetadataSources(registry)
                .addAnnotatedClass(Usuario.class)
                .addAnnotatedClass(Categoria.class)
                .addAnnotatedClass(Producto.class)
                .addAnnotatedClass(Boleta.class)
                .addAnnotatedClass(DetalleBoleta.class)
                .addAnnotatedClass(SolicitudCompra.class)
                .buildMetadata();
        metadata.validate();
    }

    @AfterAll
    static void cerrar() {
        if (registry != null) StandardServiceRegistryBuilder.destroy(registry);
        assertEquals(0, SinConexion.intentos.get(), "Ninguna prueba puede solicitar una conexion JDBC");
    }

    @Test
    void resuelveEntidadesYRelacionesSinConexion() {
        assertEquals(6, metadata.getEntityBindings().size());
        assertEquals("boleta", metadata.getEntityBinding(Boleta.class.getName()).getTable().getName());
        assertEquals("detalle_boleta", metadata.getEntityBinding(DetalleBoleta.class.getName()).getTable().getName());
        assertEquals("solicitud_compra", metadata.getEntityBinding(SolicitudCompra.class.getName()).getTable().getName());
        assertEquals(0, SinConexion.intentos.get());
    }

    @Test
    void boletaTieneVersionPeroProductoConservaSuContrato() throws Exception {
        assertNotNull(Boleta.class.getDeclaredField("version").getAnnotation(Version.class));
        assertTrue(Arrays.stream(Producto.class.getDeclaredFields())
                .noneMatch(campo -> campo.isAnnotationPresent(Version.class)));
    }

    @Test
    void relacionesConCatalogoYUsuarioNoPropaganEliminaciones() throws Exception {
        for (Class<?> entidad : Set.of(Boleta.class, DetalleBoleta.class, SolicitudCompra.class)) {
            for (var campo : entidad.getDeclaredFields()) {
                var muchos = campo.getAnnotation(ManyToOne.class);
                var uno = campo.getAnnotation(OneToOne.class);
                if (muchos != null) assertEquals(0, muchos.cascade().length);
                if (uno != null) assertEquals(0, uno.cascade().length);
            }
        }
        var detalles = Boleta.class.getDeclaredField("detalles").getAnnotation(OneToMany.class);
        assertFalse(detalles.orphanRemoval());
        assertArrayEquals(new CascadeType[]{CascadeType.PERSIST}, detalles.cascade());
        assertFalse(DetalleBoleta.class.getDeclaredField("producto").getAnnotation(JoinColumn.class).updatable());
    }

    @Test
    void conservaIndicesUnicosEImportesDecimales() {
        var tabla = metadata.getEntityBinding(DetalleBoleta.class.getName()).getTable();
        assertTrue(tabla.getUniqueKeys().values().stream().anyMatch(clave ->
                clave.getColumns().stream().map(org.hibernate.mapping.Column::getName)
                        .collect(Collectors.toSet()).equals(Set.of("boleta_id", "producto_id"))));
        var importes = metadata.getEntityBinding(Boleta.class.getName()).getTable().getColumns().stream()
                .filter(columna -> Set.of("subtotal", "ahorro", "total").contains(columna.getName())).toList();
        assertEquals(3, importes.size());
        importes.forEach(columna -> {
            assertEquals(16, columna.getPrecision());
            assertEquals(2, columna.getScale());
            assertFalse(columna.isNullable());
        });
    }

    @Test
    void columnasYReferenciasJpaCoincidenConScriptsPropuestos() throws Exception {
        Class<?>[] entidades = {Boleta.class, DetalleBoleta.class, SolicitudCompra.class};
        String[] scripts = {"V3__crear_boleta.sql", "V4__crear_detalle_boleta.sql", "V5__crear_solicitud_compra.sql"};
        for (int i = 0; i < entidades.length; i++) {
            String sql;
            try (var recurso = getClass().getResourceAsStream("/db/migration/" + scripts[i])) {
                assertNotNull(recurso);
                sql = new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
            }
            var tabla = metadata.getEntityBinding(entidades[i].getName()).getTable();
            Set<String> columnasSql = sql.lines().filter(linea -> linea.matches(
                            "    [a-z_]+ (BIGINT|VARCHAR|DECIMAL|INT|BIT|DATETIME).*"))
                    .map(linea -> linea.trim().split(" ")[0]).collect(Collectors.toSet());
            Set<String> columnasJpa = tabla.getColumns().stream()
                    .map(org.hibernate.mapping.Column::getName).collect(Collectors.toSet());
            assertEquals(columnasJpa, columnasSql, scripts[i]);
            assertTrue(sql.contains("CREATE TABLE " + tabla.getName() + " ("));
            for (var fk : tabla.getForeignKeys().values()) {
                assertTrue(sql.contains("REFERENCES " + fk.getReferencedTable().getName() + "(id)"));
            }
        }
    }

    public static class SinConexion implements ConnectionProvider {
        static final AtomicInteger intentos = new AtomicInteger();
        @Override public Connection getConnection() {
            intentos.incrementAndGet();
            throw new AssertionError("Acceso JDBC prohibido en pruebas de metadatos");
        }
        @Override public void closeConnection(Connection connection) {
            throw new AssertionError("No debe existir una conexion");
        }
        @Override public boolean supportsAggressiveRelease() { return false; }
        @Override public boolean isUnwrappableAs(Class<?> tipo) { return tipo.isInstance(this); }
        @Override public <T> T unwrap(Class<T> tipo) { return tipo.cast(this); }
    }
}
