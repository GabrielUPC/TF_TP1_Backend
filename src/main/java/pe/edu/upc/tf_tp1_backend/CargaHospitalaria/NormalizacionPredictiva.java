package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import java.text.Normalizer;
import java.util.Locale;

/** Normalización compatible con src.datos_raw.limpiar_texto del modelo. */
public final class NormalizacionPredictiva {
    private NormalizacionPredictiva() {}

    public static String texto(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    public static String servicioClave(String valor) {
        // Conserva la tilde de la ene para no fusionar nombres distintos.
        String sinAcentosVocalicos = Normalizer.normalize(texto(valor), Normalizer.Form.NFD)
                .replaceAll("[\\u0300\\u0301\\u0302\\u0308]", "");
        return Normalizer.normalize(sinAcentosVocalicos, Normalizer.Form.NFC);
    }

    public static String renipress(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (!limpio.matches("[0-9]{1,8}(?:\\.0+)?")) {
            throw new IllegalArgumentException("RENIPRESS debe contener entre 1 y 8 dígitos.");
        }
        String digitos = limpio.replaceFirst("\\.0+$", "");
        return "0".repeat(8 - digitos.length()) + digitos;
    }

    public static boolean servicioEnAlcance(String idHospitalizacion) {
        String id = texto(idHospitalizacion);
        return id.startsWith("24") || id.startsWith("25");
    }
}
