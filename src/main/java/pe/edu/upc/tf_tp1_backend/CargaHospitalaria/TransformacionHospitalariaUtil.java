package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import pe.edu.upc.tf_tp1_backend.DTOS.ErrorValidacionDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

final class TransformacionHospitalariaUtil {

    private static final int MAX_ERRORES_DETALLADOS = 100;

    private TransformacionHospitalariaUtil() {
    }

    static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    static boolean esSinInformacion(String valor) {
        return "NE_0001".equalsIgnoreCase(texto(valor));
    }

    static Integer enteroNoNegativo(String valor) {
        String limpio = texto(valor);
        if (limpio.isBlank() || esSinInformacion(limpio)) {
            return null;
        }

        try {
            String normalizado = limpio.replace(",", "");
            BigDecimal numero = new BigDecimal(normalizado);
            int entero = numero.intValueExact();
            return entero >= 0 ? entero : null;
        } catch (ArithmeticException | NumberFormatException error) {
            return null;
        }
    }

    static boolean coincideIpress(String codigo, String codigoEsperado) {
        return texto(codigo).equalsIgnoreCase(texto(codigoEsperado));
    }

    static boolean periodoValido(Integer anio, Integer mes) {
        return anio != null
                && anio >= 2000
                && anio <= 2100
                && mes != null
                && mes >= 1
                && mes <= 12;
    }

    static void agregarError(
            List<ErrorValidacionDTO> errores,
            int fila,
            String campo,
            String tipo,
            String descripcion,
            String recomendacion
    ) {
        if (errores.size() >= MAX_ERRORES_DETALLADOS) {
            return;
        }

        ErrorValidacionDTO error = new ErrorValidacionDTO();
        error.setFila(fila);
        error.setCampo(campo);
        error.setTipoError(tipo);
        error.setDescripcion(descripcion);
        error.setRecomendacion(recomendacion);
        errores.add(error);
    }

    static String claveNormalizada(String valor) {
        return texto(valor).toLowerCase(Locale.ROOT);
    }
}
