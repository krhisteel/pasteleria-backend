package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.BoletaResponse;
import cl.duoc.pasteleria.dto.CompraRequest;
import cl.duoc.pasteleria.dto.ItemCompraRequest;
import cl.duoc.pasteleria.entity.Producto;
import cl.duoc.pasteleria.entity.Rol;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.exception.ConflictoException;
import cl.duoc.pasteleria.exception.ReglaNegocioException;
import cl.duoc.pasteleria.repository.BoletaRepository;
import cl.duoc.pasteleria.repository.ProductoRepository;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock BoletaRepository boletaRepository;
    @Mock ProductoRepository productoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Spy BeneficioService beneficioService = new BeneficioService();
    @InjectMocks BoletaService boletaService;

    private Usuario cliente(LocalDate fechaNacimiento) {
        return Usuario.builder().id(3L).correo("cliente@gmail.com").rol(Rol.CLIENTE)
                .fechaNacimiento(fechaNacimiento).build();
    }

    private Producto producto(int precio, int descuento, int stock) {
        return Producto.builder().id(10L).codigo("TC002").nombre("Torta de Frutas")
                .precio(BigDecimal.valueOf(precio)).descuento(BigDecimal.valueOf(descuento))
                .stock(stock).activo(true).build();
    }

    private CompraRequest compra(int cantidad) {
        return new CompraRequest(List.of(new ItemCompraRequest(10L, cantidad)),
                "Cliente Demo", null, "Calle 123", "Valparaíso", "Viña del Mar", null, "webpay");
    }

    @Test
    void calculaTotalesConElMayorDescuentoYDescuentaStock() {
        // Usuario de 56 años (50%) y producto con oferta de 15%: se aplica el mayor, 50%.
        Producto torta = producto(50000, 15, 10);
        when(usuarioRepository.findByCorreoIgnoreCase("cliente@gmail.com"))
                .thenReturn(Optional.of(cliente(LocalDate.of(1970, 1, 1))));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(torta));
        when(boletaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse boleta = boletaService.comprar("cliente@gmail.com", compra(2));

        assertEquals(100000, boleta.subtotal());
        assertEquals(50000, boleta.total());
        assertEquals(50000, boleta.ahorro());
        assertEquals(50, boleta.productos().get(0).descuentoAplicado());
        assertEquals(8, torta.getStock()); // 10 - 2
    }

    @Test
    void sinBeneficioSeAplicaLaOfertaDelProducto() {
        Producto torta = producto(50000, 15, 10);
        when(usuarioRepository.findByCorreoIgnoreCase(any())).thenReturn(Optional.of(cliente(null)));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(torta));
        when(boletaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BoletaResponse boleta = boletaService.comprar("cliente@gmail.com", compra(1));

        assertEquals(42500, boleta.total()); // 50000 con 15% de descuento
    }

    @Test
    void rechazaStockInsuficienteYNoGuardaNada() {
        Producto torta = producto(50000, 0, 1);
        when(usuarioRepository.findByCorreoIgnoreCase(any())).thenReturn(Optional.of(cliente(null)));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(torta));

        assertThrows(ConflictoException.class, () -> boletaService.comprar("cliente@gmail.com", compra(2)));
        verify(boletaRepository, never()).save(any());
        assertEquals(1, torta.getStock()); // el stock no cambió
    }

    @Test
    void rechazaProductoInactivo() {
        Producto torta = producto(50000, 0, 10);
        torta.setActivo(false);
        when(usuarioRepository.findByCorreoIgnoreCase(any())).thenReturn(Optional.of(cliente(null)));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(torta));

        assertThrows(ReglaNegocioException.class, () -> boletaService.comprar("cliente@gmail.com", compra(1)));
        verify(boletaRepository, never()).save(any());
    }
}