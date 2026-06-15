package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsolidadorRegistrosHospitalariosTest {

    @Test
    void sumaFlujosYUsaMaximoDeCapacidadPorServicio() {
        RegistroHospitalarioImportado primero = registro(10, 8, 100, 2800);
        RegistroHospitalarioImportado segundo = registro(5, 4, 130, 4000);

        List<String> advertencias = new ArrayList<>();
        List<RegistroHospitalarioImportado> resultado =
                new ConsolidadorRegistrosHospitalarios().consolidar(
                        List.of(primero, segundo),
                        advertencias
                );

        assertEquals(1, resultado.size());
        assertEquals(15, resultado.get(0).getIngresos());
        assertEquals(12, resultado.get(0).getEgresos());
        assertEquals(130, resultado.get(0).getCamasTotales());
        assertEquals(4000, resultado.get(0).getTotalCamasDisponibles());
        assertEquals(1, advertencias.size());
    }

    @Test
    void mantieneSeparadosLosServiciosDeLaMismaIpressYPeriodo() {
        RegistroHospitalarioImportado uci = registro(10, 8, 100, 2800);
        uci.setServicioHospitalario("UCI");
        RegistroHospitalarioImportado medicina = registro(20, 18, 100, 2800);
        medicina.setServicioHospitalario("MEDICINA");

        List<RegistroHospitalarioImportado> resultado =
                new ConsolidadorRegistrosHospitalarios().consolidar(
                        List.of(uci, medicina),
                        new ArrayList<>()
                );

        assertEquals(2, resultado.size());
        assertEquals("UCI", resultado.get(0).getServicioHospitalario());
        assertEquals("MEDICINA", resultado.get(1).getServicioHospitalario());
    }

    private RegistroHospitalarioImportado registro(
            int ingresos,
            int egresos,
            int camas,
            int camasDia
    ) {
        RegistroHospitalarioImportado registro =
                new RegistroHospitalarioImportado();
        registro.setCodigoIpress("00007636");
        registro.setAnio(2026);
        registro.setMes(1);
        registro.setIdHospitalizacion("241800");
        registro.setServicioHospitalario("HOSPITALIZACION GENERAL");
        registro.setIngresos(ingresos);
        registro.setEgresos(egresos);
        registro.setEstancias(100);
        registro.setPacientesCama(200);
        registro.setCamasTotales(camas);
        registro.setTotalCamasDisponibles(camasDia);
        registro.setFallecidos(1);
        return registro;
    }
}
