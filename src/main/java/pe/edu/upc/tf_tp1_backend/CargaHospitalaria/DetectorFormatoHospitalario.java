package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class DetectorFormatoHospitalario {

    public static final List<String> COLUMNAS_FORMATO_INTERNO = List.of(
            "codigo_ipress",
            "anio",
            "mes",
            "servicio_hospitalario",
            "ingresos",
            "egresos",
            "estancias",
            "pacientes_cama",
            "camas_totales",
            "camas_disponibles_habilitadas"
    );

    public static final List<String> COLUMNAS_DATASET_D1 = List.of(
            "anio",
            "mes",
            "ubigeo",
            "departamento",
            "provincia",
            "distrito",
            "sector",
            "categoria",
            "codigo_ipress",
            "razon_soc",
            "id_hospitalizacion",
            "servicio_hospitalario",
            "nro_total_hospit_ing",
            "nro_total_hospit_egr",
            "nro_total_estancias",
            "nro_total_pacientes_camas",
            "nro_total_camas",
            "dias_cama_disponible",
            "nro_total_fallecidos"
    );

    public ResultadoDeteccionFormato detectar(List<String> columnas) {
        Set<String> encontradas = columnas.stream()
                .map(NormalizadorColumnas::normalizar)
                .filter(columna -> !columna.isBlank())
                .collect(
                        LinkedHashSet::new,
                        Set::add,
                        Set::addAll
                );

        FormatoArchivoHospitalario formato;
        if (encontradas.containsAll(COLUMNAS_DATASET_D1)) {
            formato = FormatoArchivoHospitalario.DATASET_D1;
        } else if (encontradas.containsAll(COLUMNAS_FORMATO_INTERNO)) {
            formato = FormatoArchivoHospitalario.FORMATO_INTERNO;
        } else {
            formato = FormatoArchivoHospitalario.NO_RECONOCIDO;
        }

        return new ResultadoDeteccionFormato(
                formato,
                List.copyOf(encontradas),
                COLUMNAS_FORMATO_INTERNO,
                COLUMNAS_DATASET_D1
        );
    }
}
