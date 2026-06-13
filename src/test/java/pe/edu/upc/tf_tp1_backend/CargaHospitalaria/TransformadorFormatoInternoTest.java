package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.junit.jupiter.api.Test;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransformadorFormatoInternoTest {

    @Test
    void conservaCamasFisicasParaConversionPosterior() {
        FilaArchivoHospitalario fila = new FilaArchivoHospitalario(
                2,
                Map.of(
                        "codigo_ipress", "00007636",
                        "anio", "2026",
                        "mes", "03",
                        "servicio_hospitalario", "HOSPITALIZACION GENERAL",
                        "ingresos", "80",
                        "egresos", "70",
                        "estancias", "350",
                        "pacientes_cama", "2500",
                        "camas_totales", "100",
                        "camas_disponibles_habilitadas", "90"
                )
        );
        ContenidoArchivoHospitalario contenido =
                new ContenidoArchivoHospitalario(
                        DetectorFormatoHospitalario.COLUMNAS_FORMATO_INTERNO,
                        List.of(fila)
                );
        Ipress ipress = new Ipress();
        ipress.setCodigoRenipress("00007636");

        ResultadoTransformacionHospitalaria resultado =
                new TransformadorFormatoInterno().transformar(contenido, ipress);

        assertEquals(1, resultado.getRegistros().size());
        assertEquals(
                90,
                resultado.getRegistros().get(0).getCamasDisponiblesHabilitadas()
        );
        assertNull(resultado.getRegistros().get(0).getTotalCamasDisponibles());
    }
}
