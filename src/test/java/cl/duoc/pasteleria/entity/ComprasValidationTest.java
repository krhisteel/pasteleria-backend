package cl.duoc.pasteleria.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComprasValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll static void preparar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    @AfterAll static void cerrar() { factory.close(); }

    private Boleta boleta() {
        Boleta boleta = new Boleta();
        boleta.setUsuario(new Usuario());
        boleta.setFecha(LocalDateTime.of(2026, 1, 1, 0, 0));
        boleta.setNombre("Persona de prueba");
        boleta.setEmail("qa@example.test");
        boleta.setTelefono("+56 9 1234 5678");
        boleta.setDireccion("Direccion de prueba");
        boleta.setRegion("Valparaiso");
        boleta.setComuna("Vina del Mar");
        boleta.setMetodoPago("transferencia");
        boleta.setDescuentoUsuario(BigDecimal.ZERO);
        boleta.setSubtotal(new BigDecimal("2500.00"));
        boleta.setAhorro(new BigDecimal("250.00"));
        boleta.setTotal(new BigDecimal("2250.00"));
        return boleta;
    }

    private DetalleBoleta detalle() {
        DetalleBoleta detalle = new DetalleBoleta();
        detalle.setBoleta(boleta());
        detalle.setProducto(new Producto());
        detalle.setCodigo("QA");
        detalle.setNombre("Producto historico");
        detalle.setImagen("/images/productos/TC001.jpg");
        detalle.setCantidad(2);
        detalle.setPrecioBase(new BigDecimal("1250.00"));
        detalle.setDescuentoAplicado(new BigDecimal("10.00"));
        detalle.setPrecioFinal(new BigDecimal("1125.00"));
        detalle.setSubtotal(new BigDecimal("2250.00"));
        return detalle;
    }

    private Set<String> errores(Object objeto) {
        return validator.validate(objeto).stream()
                .map(error -> error.getPropertyPath().toString()).collect(Collectors.toSet());
    }

    @Test void admiteCompraSimuladaYDetalleHistorico() {
        assertTrue(errores(boleta()).isEmpty());
        assertTrue(errores(detalle()).isEmpty());
    }

    @Test void admiteDiferenciaDeRedondeoSinCambiarFormulaDelFrontend() {
        Boleta boleta = boleta();
        boleta.setSubtotal(new BigDecimal("1.50"));
        boleta.setTotal(new BigDecimal("2.00"));
        boleta.setAhorro(new BigDecimal("-0.50"));
        assertTrue(errores(boleta).isEmpty());
    }

    @Test void exigePropietarioDespachoImportesYPagoSimulado() {
        Boleta boleta = boleta();
        boleta.setUsuario(null);
        boleta.setNombre(" ");
        boleta.setEmail("invalido");
        boleta.setTotal(new BigDecimal("-1"));
        boleta.setDescuentoUsuario(new BigDecimal("100.01"));
        boleta.setPagoSimulado(false);
        boleta.setMetodoPago("efectivo");
        assertTrue(errores(boleta).containsAll(Set.of("usuario", "nombre", "email", "total",
                "descuentoUsuario", "pagoSimulado", "metodoPago")));
    }

    @Test void rechazaCantidadFueraDeLimitesYDecimalesExcesivos() {
        DetalleBoleta detalle = detalle();
        detalle.setCantidad(0);
        detalle.setPrecioBase(new BigDecimal("1.001"));
        detalle.setDescuentoAplicado(new BigDecimal("101"));
        assertTrue(errores(detalle).containsAll(Set.of("cantidad", "precioBase", "descuentoAplicado")));
        detalle.setCantidad(51);
        assertTrue(errores(detalle).contains("cantidad"));
        detalle.setCantidad(50);
        assertFalse(errores(detalle).contains("cantidad"));
    }

    @Test void solicitudExigeUsuarioUuidYHuellaPeroAdmiteEnlacePendiente() {
        SolicitudCompra solicitud = new SolicitudCompra();
        assertTrue(errores(solicitud).containsAll(Set.of("usuario", "clave", "huella", "fecha")));
        solicitud.setUsuario(new Usuario());
        solicitud.setClave("00000000-0000-4000-8000-000000000001");
        solicitud.setHuella("a".repeat(64));
        solicitud.setFecha(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertTrue(errores(solicitud).isEmpty());
        solicitud.setClave("clave-arbitraria");
        solicitud.setHuella("token-no-permitido");
        assertTrue(errores(solicitud).containsAll(Set.of("clave", "huella")));
    }
}
