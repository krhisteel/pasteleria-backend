package cl.duoc.pasteleria.service;

import cl.duoc.pasteleria.dto.Beneficios;
import cl.duoc.pasteleria.entity.Usuario;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

// Lógica de negocio de beneficios de la pastelería.
@Component
public class BeneficioService {

    private static final List<String> DOMINIOS_DUOC = List.of("duoc.cl", "profesor.duoc.cl", "duocuc.cl");

    public Beneficios calcular(Usuario u) {
        // 50% para personas entre 50 y 120 años.
        Integer edad = u.getFechaNacimiento() == null
                ? null
                : Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        int descuentoEdad = (edad != null && edad >= 50 && edad <= 120) ? 50 : 0;

        // 10% con el código FELICES50.
        int descuentoCodigo = "FELICES50".equalsIgnoreCase(u.getCodigoPromocional()) ? 10 : 0;

        // Torta gratis para correos de Duoc.
        String correo = u.getCorreo() == null ? "" : u.getCorreo().toLowerCase();
        String dominio = correo.contains("@") ? correo.substring(correo.indexOf('@') + 1) : "";
        boolean tortaGratisDuoc = DOMINIOS_DUOC.contains(dominio);

        List<String> lista = new ArrayList<>();
        if (descuentoEdad > 0) lista.add("50% de descuento de por vida por edad.");
        if (descuentoCodigo > 0) lista.add("10% de descuento de por vida por FELICES50.");
        if (tortaGratisDuoc) lista.add("Torta gratis en tu cumpleaños por ser de la comunidad Duoc.");

        return new Beneficios(descuentoEdad, descuentoCodigo, tortaGratisDuoc, lista);
    }
}