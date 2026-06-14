package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

public final class NormalizadorColumnas {

    private static final Map<String, String> ALIAS = Map.ofEntries(
            Map.entry("anho", "anio"),
            Map.entry("ano", "anio"),
            Map.entry("co_ipress", "codigo_ipress"),
            Map.entry("codigo_renipress", "codigo_ipress"),
            Map.entry("hospitalizacion", "servicio_hospitalario"),
            Map.entry("servicio_hospitalizacion", "servicio_hospitalario"),
            Map.entry(
                    "nro_total_camas_disponib",
                    "dias_cama_disponible"
            ),
            Map.entry(
                    "nro_total_camas_disponibles",
                    "dias_cama_disponible"
            )
    );

    private NormalizadorColumnas() {
    }

    public static String normalizar(String columna) {
        if (columna == null) {
            return "";
        }

        String sinBom = columna
                .replace("\uFEFF", "")
                .replace("\"", "")
                .trim();
        String sinTildes = Normalizer.normalize(sinBom, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String normalizada = sinTildes
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        return ALIAS.getOrDefault(normalizada, normalizada);
    }
}
