package cl.duoc.pasteleria.entity;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Comprueba las barreras de ejecucion y el contenido; NO ejecuta los scripts SQL.
class MigracionesComprasTest {
    @Test
    void hibernateValidaYFlywaySoloExisteComoPluginOptIn() throws Exception {
        Properties propiedades = new Properties();
        try (var archivo = Files.newBufferedReader(Path.of("src/main/resources/application.properties"), StandardCharsets.UTF_8)) {
            propiedades.load(archivo);
        }
        assertEquals("validate", propiedades.getProperty("spring.jpa.hibernate.ddl-auto"));
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var pom = factory.newDocumentBuilder().parse(Path.of("pom.xml").toFile());
        var xpath = XPathFactory.newInstance().newXPath();
        assertEquals("0", xpath.evaluate("count(/project/dependencies/dependency[groupId='org.flywaydb'])", pom));
        String plugin = "/project/profiles/profile[id='migraciones']/build/plugins/plugin[artifactId='flyway-maven-plugin']";
        assertEquals("1", xpath.evaluate("count(" + plugin + ")", pom));
        assertEquals("false", xpath.evaluate(plugin + "/configuration/baselineOnMigrate", pom));
        assertEquals("true", xpath.evaluate(plugin + "/configuration/cleanDisabled", pom));
        assertEquals("true", xpath.evaluate(plugin + "/configuration/validateOnMigrate", pom));
        assertEquals("0", xpath.evaluate("count(" + plugin + "/executions/execution)", pom));
        assertEquals("0", xpath.evaluate("count(/project/profiles/profile[id='migraciones']/activation)", pom));
    }

    @Test
    void estadoYMetodoDePagoSeComparanComoBinariosSinCambiarValores() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V3__crear_boleta.sql"))
                .replaceAll("(?m)--.*$", "").replaceAll("\\s+", " ");
        assertTrue(sql.contains("CONSTRAINT ck_boleta_estado CHECK (BINARY estado IN "
                + "('PENDIENTE','PROCESANDO','ENVIADO','COMPLETADO','CANCELADO'))"));
        assertTrue(sql.contains("CONSTRAINT ck_boleta_metodo CHECK (BINARY metodo_pago IN ('webpay','transferencia'))"));
        assertTrue(sql.contains("BINARY estado = 'CANCELADO'"));
        assertFalse(sql.contains("CHECK (estado IN"));
        assertFalse(sql.contains("CHECK (metodo_pago IN"));
    }

    @Test
    void migracionesNoIncluyenDatosLimpiezaNiCambiosEnLaBaseOriginal() throws Exception {
        var carpeta = Path.of("src/main/resources/db/migration");
        List<String> nombres;
        try (var archivos = Files.list(carpeta)) {
            nombres = archivos.map(archivo -> archivo.getFileName().toString()).sorted().toList();
        }
        assertEquals(List.of("V1__esquema_actual.sql", "V2__version_producto.sql", "V3__crear_boleta.sql",
                "V4__crear_detalle_boleta.sql", "V5__crear_solicitud_compra.sql"), nombres);
        for (String nombre : nombres) {
            String sql = Files.readString(carpeta.resolve(nombre)).replaceAll("(?m)--.*$", "").toUpperCase();
            assertFalse(sql.matches("(?sm).*^\\s*(INSERT|UPDATE|DELETE|DROP|TRUNCATE|REPLACE|USE)\\b.*"), nombre);
            assertFalse(sql.contains("PASTELERIA_DB"), nombre);
            assertFalse(sql.contains("IF NOT EXISTS"), "No ocultar tablas preexistentes incompatibles");
        }
        String version = Files.readString(carpeta.resolve("V2__version_producto.sql"));
        assertTrue(version.contains("ADD COLUMN version BIGINT NOT NULL DEFAULT 0"));
    }
}
