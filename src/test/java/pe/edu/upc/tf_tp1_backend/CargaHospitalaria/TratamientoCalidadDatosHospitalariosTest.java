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

    private RegistroHospitalarioImportado registro(int fila,String servicio,int pacientes,int camas,int diasCama) {
        RegistroHospitalarioImportado r=new RegistroHospitalarioImportado(); r.setNumeroFila(fila);
        r.setCodigoIpress("0001"); r.setAnio(2026); r.setMes(1); r.setServicioHospitalario(servicio);
        r.setIngresos(pacientes); r.setEgresos(0); r.setEstancias(0); r.setPacientesCama(pacientes);
        r.setCamasTotales(camas); r.setTotalCamasDisponibles(diasCama); return r;
    }
}
