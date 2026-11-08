package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizacionPredictivaTest {
    @Test void replicaTrimColapsoEspaciosYUppercaseDePython() {
        assertEquals("HOSPITALIZACIÓN DE MEDICINA INTERNA",
                NormalizacionPredictiva.texto("  Hospitalización   de\tMedicina interna  "));
    }
    @Test void renipressQuedaCanonicoA8Digitos() {
        assertEquals("00001234", NormalizacionPredictiva.renipress(" 1234.0 "));
        assertThrows(IllegalArgumentException.class, () -> NormalizacionPredictiva.renipress("12A4"));
    }
    @Test void alcanceSoloAceptaFamilias24y25() {
        assertTrue(NormalizacionPredictiva.servicioEnAlcance(" 241800 "));
        assertTrue(NormalizacionPredictiva.servicioEnAlcance("250100"));
        assertFalse(NormalizacionPredictiva.servicioEnAlcance("230100"));
    }

    @Test
    void unificaTildesMayusculasEspaciosYUnicodeDescompuestoSoloEnServicios() {
        String esperado = "HOSPITALIZACION DE MEDICINA NEUMOLOGIA";
        assertEquals(esperado, NormalizacionPredictiva.servicioClave(
                "  Hospitalización   de Medicina Neumología  "));
        assertEquals(esperado, NormalizacionPredictiva.servicioClave(
                "HOSPITALIZACION DE MEDICINA NEUMOLOGI\u0301A"));
        assertEquals("HOSPITALIZACIÓN", NormalizacionPredictiva.texto("hospitalización"));
        assertEquals("NIÑOS", NormalizacionPredictiva.servicioClave("Niños"));
    }
}
