package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertNull(resultado.getRegistros().get(0).getCamasDisponiblesHabilitadas());
    }

    @Test
    void procesaD1AntiguoConComasWindows1252YCamasDia() throws Exception {
        String csv = """
                "ANHO","MES","UBIGEO","DEPARTAMENTO","PROVINCIA","DISTRITO","SECTOR","CATEGORIA","CO_IPRESS","RAZON_SOC","ID_HOSPITALIZACION","HOSPITALIZACION","NRO_TOTAL_HOSPIT_ING","NRO_TOTAL_HOSPIT_EGR","NRO_TOTAL_ESTANCIAS","NRO_TOTAL_PACIENTES_CAMAS","NRO_TOTAL_CAMAS","NRO_TOTAL_CAMAS_DISPONIB","NRO_TOTAL_FALLECIDOS"
                "2016","02","080601","CUSCO","CANCHIS","SICUANI","ESSALUD","II-1","00010063","HOSPITAL ESSALUD SICUANI","241900","HOSPITALIZACIÓN DE MEDICINA MEDICINA INTERNA","7","7","43","0","65","1885","0"
                """;
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "ConsultaD1_Hospitalizaciones_Especialidad_2016_v1.csv",
                "text/csv",
                csv.getBytes(Charset.forName("windows-1252"))
        );

        ContenidoArchivoHospitalario contenido =
                new LectorArchivoHospitalario().leer(archivo);
        ResultadoDeteccionFormato deteccion =
                new DetectorFormatoHospitalario().detectar(
                        contenido.getColumnas()
                );

        Ipress ipress = new Ipress();
        ipress.setCodigoRenipress("00010063");
        ResultadoTransformacionHospitalaria resultado =
                new TransformadorD1().transformar(contenido, ipress);

        assertEquals(FormatoArchivoHospitalario.DATASET_D1, deteccion.getFormato());
        assertEquals(1, resultado.getRegistros().size());
        assertEquals("00010063", resultado.getRegistros().get(0).getCodigoIpress());
        assertEquals(65, resultado.getRegistros().get(0).getCamasTotales());
        assertEquals(
                1885,
                resultado.getRegistros().get(0).getTotalCamasDisponibles()
        );
        assertNull(resultado.getRegistros().get(0).getCamasDisponiblesHabilitadas());
    }
}
