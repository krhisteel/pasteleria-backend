package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.entity.*;
import cl.duoc.pasteleria.exception.*;
import cl.duoc.pasteleria.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {
    @Mock ProductoRepository productoRepository;
    @Mock CategoriaRepository categoriaRepository;
    @InjectMocks ProductoService service;

    private Producto producto() {
        return Producto.builder().id(17L).codigo("TC003").nombre("Torta")
            .descripcion("Chocolate").imagenUrl("/images/torta.jpg").precio(new BigDecimal("5000"))
            .stock(5).stockCritico(2).descuento(BigDecimal.ZERO).activo(true)
            .categoria(Categoria.builder().id(8L).nombre("Tortas").build()).build();
    }
    @Test void creaNormalizandoCodigoYDevuelveDTO() {
        Producto p = producto(); p.setCodigo(" tc003 ");
        when(categoriaRepository.findById(8L)).thenReturn(Optional.of(p.getCategoria()));
        when(productoRepository.save(any())).thenAnswer(i -> { Producto guardado = i.getArgument(0); guardado.setId(20L); return guardado; });
        var dto = service.crear(p);
        assertEquals("TC003", dto.codigo()); assertEquals(20L, dto.id()); assertEquals(8L, dto.categoria().id());
    }
    @Test void rechazaCodigoDuplicadoInclusoInactivo() {
        when(productoRepository.existsByCodigoIgnoreCase("TC003")).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.crear(producto()));
        verify(productoRepository, never()).save(any());
    }
    @Test void codigoInmutable() {
        Producto original = producto(); Producto datos = producto(); datos.setCodigo("OTRO");
        when(productoRepository.findById(17L)).thenReturn(Optional.of(original));
        assertThrows(ReglaNegocioException.class, () -> service.actualizar(17L, datos));
        assertEquals("TC003", original.getCodigo()); verify(productoRepository, never()).save(any());
    }
    @Test void actualizaSinCambiarIdentidadNiCategoriaPorNombre() {
        Producto original = producto(); Producto datos = producto(); datos.setNombre("Nombre nuevo");
        when(productoRepository.findById(17L)).thenReturn(Optional.of(original));
        when(categoriaRepository.findById(8L)).thenReturn(Optional.of(original.getCategoria()));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("Nombre nuevo", service.actualizar(17L, datos).nombre());
        assertEquals("TC003", original.getCodigo());
    }
    @Test void bajaLogicaConservaStockYRegistro() {
        Producto p = producto(); when(productoRepository.findById(17L)).thenReturn(Optional.of(p));
        service.eliminar(17L); assertFalse(p.getActivo()); assertEquals(5, p.getStock());
        verify(productoRepository).save(p); verify(productoRepository, never()).delete(any());
    }
    @Test void editarInactivoNoLoReactivaImplicitamente() {
        Producto p = producto(); p.setActivo(false);
        when(productoRepository.findById(17L)).thenReturn(Optional.of(p));
        when(categoriaRepository.findById(8L)).thenReturn(Optional.of(p.getCategoria()));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertFalse(service.actualizar(17L, producto()).activo());
    }
    @Test void detallePublicoOcultaInactivo() {
        Producto p = producto(); p.setActivo(false); when(productoRepository.findById(17L)).thenReturn(Optional.of(p));
        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(17L));
    }
    @Test void consultasPublicasSoloActivosYAdminIncluyeTodos() {
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(producto()));
        when(productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue("Torta")).thenReturn(List.of(producto()));
        Producto inactivo = producto(); inactivo.setActivo(false);
        when(productoRepository.findAll()).thenReturn(List.of(producto(), inactivo));
        assertEquals(1, service.listarActivos().size()); assertEquals(1, service.buscarPorNombre("Torta").size());
        assertEquals(2, service.listar().size());
    }
    @Test void categoriaInexistenteNoGuarda() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(producto()));
        verify(productoRepository, never()).save(any());
    }
    @Test void validaObligatoriosPrecioStockDescuentoYLongitudes() {
        List<java.util.function.Consumer<Producto>> invalidaciones = List.of(
            p -> p.setCodigo(null), p -> p.setCodigo("!"), p -> p.setNombre(" "),
            p -> p.setPrecio(BigDecimal.ZERO), p -> p.setPrecio(new BigDecimal("-1")),
            p -> p.setPrecio(new BigDecimal("1.001")), p -> p.setStock(-1), p -> p.setStockCritico(-1),
            p -> p.setDescuento(new BigDecimal("101")), p -> p.setDescuento(null),
            p -> p.setCategoria(null), p -> p.setDescripcion(""), p -> p.setImagenUrl("javascript:alert(1)"),
            p -> p.setImagenUrl("/" + "a".repeat(500)), p -> p.setNombre("a".repeat(151)));
        for (var invalidar : invalidaciones) { Producto p = producto(); invalidar.accept(p);
            assertThrows(ReglaNegocioException.class, () -> service.crear(p)); }
        verify(productoRepository, never()).save(any());
    }
}
