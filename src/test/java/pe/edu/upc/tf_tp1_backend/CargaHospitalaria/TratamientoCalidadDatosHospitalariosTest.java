package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TratamientoCalidadDatosHospitalariosTest {
    private final TratamientoCalidadDatosHospitalarios tratamiento = new TratamientoCalidadDatosHospitalarios();

    @Test void apartaGrupoCompletoAntesDeConsolidar() {
        RegistroHospitalarioImportado validoMismoGrupo = registro(1," medicina  general ",10,10,310);
        RegistroHospitalarioImportado q05 = registro(2,"MEDICINA GENERAL",1,0,0);
        RegistroHospitalarioImportado otroValido = registro(3,"CIRUGIA",5,8,248);
        ResultadoTratamientoCalidad resultado=tratamiento.aplicar(List.of(validoMismoGrupo,q05,otroValido));
        assertEquals(List.of(otroValido),resultado.registrosValidos());
        assertEquals(List.of(validoMismoGrupo,q05),resultado.registrosPendientes());
        assertEquals(1,resultado.gruposPendientes());
        assertEquals(List.of("Q05","Q06"),resultado.hallazgos().stream().map(HallazgoCalidadImportado::regla).toList());
    }

    @Test void q06AplicaAunqueExistanCamas() {
        ResultadoTratamientoCalidad r=tratamiento.aplicar(List.of(registro(4,"MEDICINA",1,20,0)));
        assertTrue(r.registrosValidos().isEmpty());
        assertEquals("Q06",r.hallazgos().get(0).regla());
    }

    @Test void ceroSinActividadNoActivaLasReglas() {
        ResultadoTratamientoCalidad r=tratamiento.aplicar(List.of(registro(5,"MEDICINA",0,0,0)));
        assertEquals(1,r.registrosValidos().size()); assertTrue(r.hallazgos().isEmpty());
    }

    @Test void formatoInternoConvierteCamasHabilitadasADiasCamaAntesDeQ08() {
        RegistroHospitalarioImportado valido = registro(8, "MEDICINA", 300, 10, 0);
        valido.setTotalCamasDisponibles(null);
        valido.setCamasDisponiblesHabilitadas(10);
        RegistroHospitalarioImportado q08 = registro(9, "CIRUGIA", 400, 10, 0);
        q08.setTotalCamasDisponibles(null);
        q08.setCamasDisponiblesHabilitadas(10);

        ResultadoTratamientoCalidad resultado = tratamiento.aplicar(List.of(valido, q08));

        assertEquals(List.of(valido), resultado.registrosValidos());
        assertEquals(List.of(q08), resultado.registrosPendientes());
        assertEquals("Q08", resultado.hallazgos().get(0).regla());
    }

    @Test void q07Yq08ApartanSinCorregirValores() {
        RegistroHospitalarioImportado q07 = registro(6, "MEDICINA", 36, 1116, 1200);
        RegistroHospitalarioImportado q08 = registro(7, "CIRUGIA", 121, 10, 100);
        ResultadoTratamientoCalidad r = tratamiento.aplicar(List.of(q07, q08));
        assertTrue(r.registrosValidos().isEmpty());
        assertEquals(List.of("Q07", "Q08"), r.hallazgos().stream()
                .map(HallazgoCalidadImportado::regla).toList());
        assertEquals(1116, q07.getCamasTotales());
        assertEquals("capacidad_q05_q06_q07_q08_v3", TratamientoCalidadDatosHospitalarios.VERSION_POLITICA);
    }

    private RegistroHospitalarioImportado registro(int fila,String servicio,int pacientes,int camas,int diasCama) {
        RegistroHospitalarioImportado r=new RegistroHospitalarioImportado(); r.setNumeroFila(fila);
        r.setCodigoIpress("0001"); r.setAnio(2026); r.setMes(1); r.setServicioHospitalario(servicio);
        r.setIngresos(pacientes); r.setEgresos(0); r.setEstancias(0); r.setPacientesCama(pacientes);
        r.setCamasTotales(camas); r.setTotalCamasDisponibles(diasCama); return r;
    }
}
