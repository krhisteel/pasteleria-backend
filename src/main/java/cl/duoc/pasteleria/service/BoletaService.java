package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.Beneficios;
import cl.duoc.pasteleria.dto.BoletaResponse;
import cl.duoc.pasteleria.dto.CompraRequest;
import cl.duoc.pasteleria.dto.ItemCompraRequest;
import cl.duoc.pasteleria.entity.Boleta;
import cl.duoc.pasteleria.entity.DetalleBoleta;
import cl.duoc.pasteleria.entity.Producto;
import cl.duoc.pasteleria.entity.Usuario;
import cl.duoc.pasteleria.exception.ConflictoException;
import cl.duoc.pasteleria.exception.RecursoNoEncontradoException;
import cl.duoc.pasteleria.exception.ReglaNegocioException;
import cl.duoc.pasteleria.repository.BoletaRepository;
import cl.duoc.pasteleria.repository.ProductoRepository;
import cl.duoc.pasteleria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BoletaService {

    private static final Set<String> ESTADOS_PERMITIDOS =
            Set.of("pendiente", "procesando", "enviado", "completado", "cancelado");

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final BeneficioService beneficioService;

    @Transactional
    public BoletaResponse comprar(String correo, CompraRequest req) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

        Beneficios beneficios = beneficioService.calcular(usuario);
        int descuentoUsuario = Math.max(beneficios.descuentoEdad(), beneficios.descuentoCodigo());

        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario);
        boleta.setNombre(req.nombre().trim());
        boleta.setTelefono(req.telefono());
        boleta.setDireccion(req.direccion().trim());
        boleta.setRegion(req.region());
        boleta.setComuna(req.comuna());
        boleta.setIndicaciones(req.indicaciones());
        boleta.setMetodoPago(req.metodoPago());
        boleta.setDescuentoUsuario(descuentoUsuario);

        int subtotal = 0;
        int total = 0;
        Set<Long> revisados = new HashSet<>();

        for (ItemCompraRequest item : req.items()) {
            if (!revisados.add(item.productoId())) {
                throw new ReglaNegocioException("Hay productos repetidos en el carrito.");
            }

            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "El producto " + item.productoId() + " no existe."));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new ReglaNegocioException("\"" + producto.getNombre() + "\" ya no está disponible.");
            }
            if (item.cantidad() > producto.getStock()) {
                throw new ConflictoException("Solo quedan " + producto.getStock()
                        + " unidades de \"" + producto.getNombre() + "\".");
            }

            int precioBase = producto.getPrecio().intValue();
            int descuentoProducto = producto.getDescuento() == null ? 0 : producto.getDescuento().intValue();
            int descuento = Math.max(descuentoProducto, descuentoUsuario);
            int precioFinal = (int) Math.round(precioBase * (1 - descuento / 100.0));

            producto.setStock(producto.getStock() - item.cantidad());

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setBoleta(boleta);
            detalle.setProducto(producto);
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioBase(precioBase);
            detalle.setDescuentoAplicado(descuento);
            detalle.setPrecioFinal(precioFinal);
            detalle.setSubtotal(precioFinal * item.cantidad());
            boleta.getDetalles().add(detalle);

            subtotal += precioBase * item.cantidad();
            total += precioFinal * item.cantidad();
        }

        boleta.setSubtotal(subtotal);
        boleta.setTotal(total);
        boleta.setAhorro(subtotal - total);

        return BoletaResponse.from(boletaRepository.save(boleta));
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> misBoletas(String correo) {
        List<BoletaResponse> lista = new ArrayList<>();
        for (Boleta b : boletaRepository.findByUsuarioCorreoIgnoreCaseOrderByFechaDesc(correo)) {
            lista.add(BoletaResponse.from(b));
        }
        return lista;
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar() {
        List<BoletaResponse> lista = new ArrayList<>();
        for (Boleta b : boletaRepository.findAllByOrderByFechaDesc()) {
            lista.add(BoletaResponse.from(b));
        }
        return lista;
    }

    @Transactional(readOnly = true)
    public BoletaResponse obtener(Long id) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada."));
        return BoletaResponse.from(boleta);
    }

    // Actualiza el estado de una orden (solo ADMIN). Si pasa a "cancelado", devuelve el stock.
    @Transactional
    public BoletaResponse actualizarEstado(Long id, String nuevoEstado) {
        String estadoNormalizado = nuevoEstado == null ? "" : nuevoEstado.trim().toLowerCase();

        if (!ESTADOS_PERMITIDOS.contains(estadoNormalizado)) {
            throw new ReglaNegocioException("El estado seleccionado no es válido.");
        }

        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada."));

        String estadoActual = boleta.getEstado() == null ? "pendiente" : boleta.getEstado();

        if (estadoActual.equals(estadoNormalizado)) {
            return BoletaResponse.from(boleta);
        }

        if ("cancelado".equals(estadoActual)) {
            throw new ReglaNegocioException(
                    "Una orden cancelada no se puede reactivar. Debe realizarse una compra nueva."
            );
        }

        if ("cancelado".equals(estadoNormalizado)) {
            for (DetalleBoleta detalle : boleta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
            }
        }

        boleta.setEstado(estadoNormalizado);
        return BoletaResponse.from(boletaRepository.save(boleta));
    }
}