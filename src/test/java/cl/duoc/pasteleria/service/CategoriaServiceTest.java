package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.entity.Categoria;
import cl.duoc.pasteleria.exception.*;
import cl.duoc.pasteleria.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
    @Mock CategoriaRepository categoriaRepository;
    @Mock ProductoRepository productoRepository;
    @InjectMocks CategoriaService service;
    private Categoria categoria() { return Categoria.builder().id(8L).nombre("Tortas").build(); }
    @Test void impideEliminarConProductosActivosOInactivos() {
        when(categoriaRepository.findById(8L)).thenReturn(Optional.of(categoria()));
        when(productoRepository.existsByCategoriaId(8L)).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.eliminar(8L));
        verify(categoriaRepository, never()).delete(any());
    }
    @Test void eliminaSoloCategoriaSinAsociaciones() {
        Categoria c = categoria(); when(categoriaRepository.findById(8L)).thenReturn(Optional.of(c));
        service.eliminar(8L); verify(categoriaRepository).delete(c); verify(categoriaRepository).flush();
    }
    @Test void validaNombreYDuplicados() {
        assertThrows(ReglaNegocioException.class, () -> service.crear(Categoria.builder().nombre(" ").build()));
        assertThrows(ReglaNegocioException.class, () -> service.crear(Categoria.builder().nombre("ab").build()));
        when(categoriaRepository.existsByNombreIgnoreCase("Tortas")).thenReturn(true);
        assertThrows(ConflictoException.class, () -> service.crear(categoria()));
    }
    @Test void renombraSinReescribirProductos() {
        Categoria c = categoria(); when(categoriaRepository.findById(8L)).thenReturn(Optional.of(c));
        when(categoriaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("Postres", service.actualizar(8L, Categoria.builder().nombre("Postres").build()).getNombre());
        assertEquals(8L, c.getId()); verifyNoInteractions(productoRepository);
    }
    @Test void inexistenteEs404DeNegocio() { assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99L)); }
}
