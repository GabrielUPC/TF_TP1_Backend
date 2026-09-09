package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardDetalleDTO;
import pe.edu.upc.tf_tp1_backend.Entities.*;
import pe.edu.upc.tf_tp1_backend.Repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceImplementsTest {
    private IIndicadorHospitalarioRepository indicadores;
    private IPrediccionRiesgoRepository predicciones;
    private DashboardServiceImplements servicio;
    private IndicadorHospitalario indicador;

    @BeforeEach void preparar() {
        indicadores = mock(IIndicadorHospitalarioRepository.class);
        predicciones = mock(IPrediccionRiesgoRepository.class);
        IUsuarioRepository usuarios = mock(IUsuarioRepository.class);
        servicio = new DashboardServiceImplements(indicadores, predicciones, usuarios);
        Ipress ipress = new Ipress(); ipress.setIdIpress(1L); ipress.setCodigoRenipress("00007636");
        Rol rol = new Rol(); rol.setNombreRol("ATENCION_HOSPITALIZACION");
        Usuario usuario = new Usuario(); usuario.setEstado(true); usuario.setRol(rol); usuario.setIpress(ipress);
        when(usuarios.findByCorreo("hospital@correo.pe")).thenReturn(Optional.of(usuario));
        indicador = indicador(ipress);
        when(indicadores.buscarPorIpress(1L)).thenReturn(List.of(indicador));
    }

    @Test void muestraIndicadoresAunqueNoExistaPrediccion() {
        when(predicciones.findByIndicadorHospitalario_IdIndicador(1)).thenReturn(Optional.empty());
        List<DashboardDetalleDTO> r = servicio.obtenerDetalleGeneral("hospital@correo.pe", null);
        assertEquals(1, r.size()); assertEquals(240, r.get(0).getPacientesCama());
        assertNull(r.get(0).getIdPrediccion());
        assertEquals(1, servicio.obtenerResumenGeneral("hospital@correo.pe", null).getTotalRegistros());
        assertEquals(0, servicio.obtenerResumenGeneral("hospital@correo.pe", null).getTotalPredicciones());
    }

    @Test void adjuntaSoloPrediccionBinariaVigente() {
        PrediccionRiesgo p = prediccion(indicador, true, "modelo_ipress_binario_v1");
        when(predicciones.findByIndicadorHospitalario_IdIndicador(1)).thenReturn(Optional.of(p));
        DashboardDetalleDTO d = servicio.obtenerDetalleGeneral("hospital@correo.pe", null).get(0);
        assertEquals(.41, d.getProbabilidadAltaPresion());
        assertEquals(.36, d.getThreshold()); assertTrue(d.getAlertaAltaPresion());
        assertEquals("2025-07", d.getPeriodoPredicho());
    }

    @Test void omitePrediccionLegacyOInactivaPeroConservaHistorico() {
        when(predicciones.findByIndicadorHospitalario_IdIndicador(1))
                .thenReturn(Optional.of(prediccion(indicador, true, "XGBoost - FastAPI")));
        DashboardDetalleDTO d = servicio.obtenerDetalleGeneral("hospital@correo.pe", null).get(0);
        assertNull(d.getIdPrediccion()); assertEquals(10, d.getCamasTotales());
    }

    @Test void alertaSeDeduplicaPorServicioPeriodoYModelo() {
        PrediccionRiesgo p = prediccion(indicador, true, "modelo_ipress_binario_v1");
        when(predicciones.findByIndicadorHospitalario_IdIndicador(1)).thenReturn(Optional.of(p));
        assertEquals(1, servicio.obtenerAlertas("hospital@correo.pe", null).size());
    }

    private IndicadorHospitalario indicador(Ipress ipress) {
        ArchivoCargado a = new ArchivoCargado(); a.setIdArchivo(5L); a.setNombreArchivo("D1.csv"); a.setIpress(ipress);
        RegistroHospitalario r = new RegistroHospitalario(); r.setIdRegistro(1); r.setArchivoCargado(a);
        r.setAnio(2025); r.setMes(6); r.setServicioHospitalario("MEDICINA");
        r.setIngresos(20); r.setEgresos(18); r.setEstancias(120); r.setPacientesCama(240);
        r.setCamasTotales(10); r.setTotalCamasDisponibles(300);
        IndicadorHospitalario i = new IndicadorHospitalario(); i.setIdIndicador(1); i.setRegistroHospitalario(r);
        i.setOcupacionEstimada(.8); i.setPresionIngresosCamas(2.0);
        i.setPromedioEstancia(6.6); i.setRotacionCamas(1.8); return i;
    }
    private PrediccionRiesgo prediccion(IndicadorHospitalario i, boolean vigente, String modelo) {
        PrediccionRiesgo p = new PrediccionRiesgo(); p.setIdPrediccion(7); p.setIndicadorHospitalario(i);
        p.setProbabilidadAltaPresion(.41); p.setThreshold(.36); p.setAlertaAltaPresion(true);
        p.setNivelRiesgo("ALTO"); p.setDescripcionRiesgo("Alta presion");
        p.setPeriodoPredicho("2025-07"); p.setModeloUtilizado(modelo);
        p.setFechaPrediccion(LocalDateTime.now()); p.setVigente(vigente); return p;
    }
}
