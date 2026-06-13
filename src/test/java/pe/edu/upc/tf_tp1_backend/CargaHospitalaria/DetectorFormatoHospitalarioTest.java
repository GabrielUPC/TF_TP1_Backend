package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DetectorFormatoHospitalarioTest {

    private final DetectorFormatoHospitalario detector =
            new DetectorFormatoHospitalario();

    @Test
    void detectaDatasetD1PorColumnas() {
        ResultadoDeteccionFormato resultado = detector.detectar(
                DetectorFormatoHospitalario.COLUMNAS_DATASET_D1
        );

        assertEquals(
                FormatoArchivoHospitalario.DATASET_D1,
                resultado.getFormato()
        );
    }

    @Test
    void detectaFormatoInternoPorColumnas() {
        ResultadoDeteccionFormato resultado = detector.detectar(
                DetectorFormatoHospitalario.COLUMNAS_FORMATO_INTERNO
        );

        assertEquals(
                FormatoArchivoHospitalario.FORMATO_INTERNO,
                resultado.getFormato()
        );
    }

    @Test
    void rechazaFormatoDesconocido() {
        ResultadoDeteccionFormato resultado = detector.detectar(
                List.of("nombre", "valor", "fecha")
        );

        assertEquals(
                FormatoArchivoHospitalario.NO_RECONOCIDO,
                resultado.getFormato()
        );
    }
}
