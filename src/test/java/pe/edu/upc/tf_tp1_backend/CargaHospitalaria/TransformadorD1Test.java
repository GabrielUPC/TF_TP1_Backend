package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformadorD1Test {

    @Test
    void preservaCodigoDescartaNe0001YConservaCamasDia() throws Exception {
        String csv = """
                "ANHO";"MES";"UBIGEO";"DEPARTAMENTO";"PROVINCIA";"DISTRITO";"SECTOR";"CATEGORIA";"CO_IPRESS";"RAZON_SOC";"ID_HOSPITALIZACION";"HOSPITALIZACION";"NRO_TOTAL_HOSPIT_ING";"NRO_TOTAL_HOSPIT_EGR";"NRO_TOTAL_ESTANCIAS";"NRO_TOTAL_PACIENTES_CAMAS";"NRO_TOTAL_CAMAS";"DIAS_CAMA_DISPONIBLE";"NRO_TOTAL_FALLECIDOS"
                "2026";"01";"150101";"LIMA";"LIMA";"LIMA";"MINSA";"III-1";"00007636";"IPRESS PRUEBA";"241800";"HOSPITALIZACION GENERAL";"69";"67";"701";"704";"13";"715";"2"
                "2026";"01";"150101";"LIMA";"LIMA";"LIMA";"MINSA";"III-1";"00007636";"IPRESS PRUEBA";"NE_0001";"";"NE_0001";"NE_0001";"NE_0001";"NE_0001";"NE_0001";"NE_0001";"NE_0001"
                """;
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "ConsultaD1_2026_v8.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        ContenidoArchivoHospitalario contenido =
                new LectorArchivoHospitalario().leer(archivo);
        ResultadoDeteccionFormato deteccion =
                new DetectorFormatoHospitalario().detectar(
                        contenido.getColumnas()
                );

        Ipress ipress = new Ipress();
        ipress.setCodigoRenipress("00007636");
        ResultadoTransformacionHospitalaria resultado =
                new TransformadorD1().transformar(contenido, ipress);

        assertEquals(FormatoArchivoHospitalario.DATASET_D1, deteccion.getFormato());
        assertEquals(1, resultado.getRegistros().size());
        assertEquals(1, resultado.getTotalFilasInvalidas());
        assertEquals("00007636", resultado.getRegistros().get(0).getCodigoIpress());
        assertEquals(1, resultado.getRegistros().get(0).getMes());
        assertEquals(715, resultado.getRegistros().get(0).getTotalCamasDisponibles());
        assertEquals(null, resultado.getRegistros().get(0).getCamasDisponiblesHabilitadas());
    }
}
