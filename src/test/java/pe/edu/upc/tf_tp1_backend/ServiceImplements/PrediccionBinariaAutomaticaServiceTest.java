package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pe.edu.upc.tf_tp1_backend.DTOS.*;
import pe.edu.upc.tf_tp1_backend.Entities.*;
import pe.edu.upc.tf_tp1_backend.Enums.NivelRiesgoPresentacion;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;
import pe.edu.upc.tf_tp1_backend.Repositories.*;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PrediccionBinariaAutomaticaServiceTest {
    private IUsuarioRepository usuarios;
    private IRegistroHospitalarioRepository registros;
    private IIndicadorHospitalarioRepository indicadores;
    private IPrediccionRiesgoRepository predicciones;
    private CalidadInferenciaRepository calidad;
    private ModeloPredictivoClientService modelo;
    private IReporteInterfaces reportes;
    private PrediccionBinariaAutomaticaService servicio;
    private Ipress ipress;

    @BeforeEach
    void preparar() {
        usuarios = mock(IUsuarioRepository.class); registros = mock(IRegistroHospitalarioRepository.class);
        indicadores = mock(IIndicadorHospitalarioRepository.class); predicciones = mock(IPrediccionRiesgoRepository.class);
        calidad = mock(CalidadInferenciaRepository.class); modelo = mock(ModeloPredictivoClientService.class);
        reportes = mock(IReporteInterfaces.class);
        servicio = new PrediccionBinariaAutomaticaService(
                usuarios, registros, indicadores, predicciones, calidad, modelo, reportes);
        ipress = new Ipress(10L, "00005617", "IPRESS", "II-2", "150101", "LIMA", "LIMA", "LIMA");
        Rol rol = new Rol(); rol.setNombreRol("ATENCION_HOSPITALIZACION");
        Usuario usuario = new Usuario(); usuario.setCorreo("hospital@prueba.pe"); usuario.setEstado(true);
        usuario.setRol(rol); usuario.setIpress(ipress);
        when(usuarios.findByCorreo("hospital@prueba.pe")).thenReturn(Optional.of(usuario));
        when(calidad.existePendiente(anyLong(), anyString(), any(), any())).thenReturn(false);
        when(registros.buscarPorIpress(10L)).thenReturn(ventana());
        when(modelo.predecirBinario(any())).thenReturn(respuesta());
        when(indicadores.findByRegistroHospitalario_IdRegistro(anyInt()))
                .thenAnswer(i -> Optional.of(indicador(i.getArgument(0))));
        when(predicciones.findByIndicadorHospitalario_IdIndicador(anyInt())).thenReturn(Optional.empty());
        when(predicciones.findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_Ipress_IdIpress(10L))
                .thenReturn(List.of());
        when(predicciones.save(any(PrediccionRiesgo.class))).thenAnswer(invocacion -> {
            PrediccionRiesgo prediccion = invocacion.getArgument(0);
            if (prediccion.getIdPrediccion() == null) prediccion.setIdPrediccion(77);
            return prediccion;
        });
    }

    @Test
    void usaIpressDelJwtEnviaCincoHistoricosLlamaUnaVezYPersiste() {
        ModeloPrediccionBinariaResponseDTO r = servicio.predecir(solicitud(), "hospital@prueba.pe");
        assertEquals(0.41, r.getProbabilidadAltaPresion());
        ArgumentCaptor<ModeloPrediccionRequestDTO> request = ArgumentCaptor.forClass(ModeloPrediccionRequestDTO.class);
        verify(modelo, times(1)).predecirBinario(request.capture());
        assertEquals(5, request.getValue().getHistorialUltimosMeses().size());
        assertEquals(List.of(1,2,3,4,5), request.getValue().getHistorialUltimosMeses().stream()
                .map(ModeloDatosHospitalariosDTO::getMes).toList());
        assertEquals(6, request.getValue().getRegistroActual().getMes());
        ArgumentCaptor<PrediccionRiesgo> guardada = ArgumentCaptor.forClass(PrediccionRiesgo.class);
        verify(predicciones).save(guardada.capture());
        assertEquals("modelo_ipress_binario_v1", guardada.getValue().getModeloUtilizado());
        assertEquals("2025-07", guardada.getValue().getPeriodoPredicho());
        assertTrue(guardada.getValue().getAlertaAltaPresion());
        verify(reportes).generarReporte(eq("hospital@prueba.pe"), argThat(dto ->
                dto != null && Objects.equals(dto.getIdPrediccion(), guardada.getValue().getIdPrediccion())));
    }

    @Test
    void usuarioNoPuedeElegirOtraIpress() {
        servicio.predecir(solicitud(), "hospital@prueba.pe");
        verify(registros).buscarPorIpress(10L);
        verify(registros, never()).buscarPorIpress(argThat(id -> id != null && id != 10L));
    }

    @Test
    void huecoIntermedioNoLlegaAFastApi() {
        List<RegistroHospitalario> v = new ArrayList<>(ventana()); v.remove(2);
        when(registros.buscarPorIpress(10L)).thenReturn(v);
        assertCodigo("MES_INTERMEDIO_FALTANTE", () -> servicio.predecir(solicitud(), "hospital@prueba.pe"));
        verifyNoInteractions(modelo);
    }

    @Test
    void pendienteCalidadNoLlegaAFastApiNiCatalogo() {
        when(calidad.existePendiente(eq(10L), eq("MEDICINA"), any(), any())).thenReturn(true);
        assertCodigo("CALIDAD_PENDIENTE", () -> servicio.predecir(solicitud(), "hospital@prueba.pe"));
        assertTrue(servicio.catalogo("hospital@prueba.pe").getServicios().isEmpty());
        verifyNoInteractions(modelo);
    }

    @Test
    void duplicadosIdenticosEligenUltimaVersionValidada() {
        List<RegistroHospitalario> v = new ArrayList<>(ventana());
        RegistroHospitalario ultima = registro(2025, 3, 99);
        ultima.getArchivoCargado().setFechaCarga(LocalDateTime.of(2026, 1, 1, 0, 0));
        v.add(ultima); when(registros.buscarPorIpress(10L)).thenReturn(v);
        servicio.predecir(solicitud(), "hospital@prueba.pe");
        ArgumentCaptor<ModeloPrediccionRequestDTO> captor = ArgumentCaptor.forClass(ModeloPrediccionRequestDTO.class);
        verify(modelo).predecirBinario(captor.capture());
        assertEquals(3, captor.getValue().getHistorialUltimosMeses().get(2).getMes());
    }

    @Test
    void duplicadosDiferentesBloqueanSinElegirArbitrariamente() {
        List<RegistroHospitalario> v = new ArrayList<>(ventana());
        RegistroHospitalario conflicto = registro(2025, 3, 99); conflicto.setIngresos(999); v.add(conflicto);
        when(registros.buscarPorIpress(10L)).thenReturn(v);
        PrediccionBinariaAutomaticaException e = assertCodigo(
                "PERIODO_CONFLICTIVO", () -> servicio.predecir(solicitud(), "hospital@prueba.pe"));
        assertEquals(409, e.getStatus().value()); verifyNoInteractions(modelo);
    }

    @Test
    void catalogoSoloPublicaPeriodoConSeisMesesValidos() {
        CatalogoPrediccionBinariaDTO c = servicio.catalogo("hospital@prueba.pe");
        assertEquals("00005617", c.getCodigoIpress());
        assertEquals(List.of("2025-06"), c.getServicios().get(0).getPeriodosDisponibles());
    }

    @Test
    void servicioFueraDe24y25SeRechaza() {
        List<RegistroHospitalario> v = ventana(); v.forEach(r -> r.setIdHospitalizacion("220100"));
        when(registros.buscarPorIpress(10L)).thenReturn(v);
        assertCodigo("SERVICIO_FUERA_ALCANCE", () -> servicio.predecir(solicitud(), "hospital@prueba.pe"));
    }

    @Test
    void usuarioSinRolHospitalarioRecibe403() {
        Usuario u = usuarios.findByCorreo("hospital@prueba.pe").orElseThrow();
        u.getRol().setNombreRol("ADMISION_REGISTROS");
        PrediccionBinariaAutomaticaException e = assertCodigo(
                "IPRESS_NO_AUTORIZADA", () -> servicio.predecir(solicitud(), "hospital@prueba.pe"));
        assertEquals(403, e.getStatus().value());
    }

    private PrediccionBinariaAutomaticaRequestDTO solicitud() {
        PrediccionBinariaAutomaticaRequestDTO d = new PrediccionBinariaAutomaticaRequestDTO();
        d.setServicio("  medicina "); d.setPeriodoActual("2025-06"); return d;
    }
    private List<RegistroHospitalario> ventana() {
        List<RegistroHospitalario> v = new ArrayList<>();
        for (int m=1;m<=6;m++) v.add(registro(2025,m,m));
        return v;
    }
    private RegistroHospitalario registro(int anio, int mes, long idArchivo) {
        ArchivoCargado a = new ArchivoCargado(); a.setIdArchivo(idArchivo); a.setIpress(ipress);
        a.setEstadoValidacion("VALIDADO"); a.setFechaCarga(LocalDateTime.of(2025, mes, 1, 0, 0));
        RegistroHospitalario r = new RegistroHospitalario(); r.setIdRegistro(anio*100+mes+(int)idArchivo);
        r.setArchivoCargado(a); r.setAnio(anio); r.setMes(mes); r.setServicioHospitalario("MEDICINA");
        r.setIdHospitalizacion("241800"); r.setSector("MINSA"); r.setIngresos(20); r.setEgresos(18);
        r.setEstancias(120); r.setPacientesCama(240); r.setCamasTotales(10);
        r.setTotalCamasDisponibles(300); r.setFallecidos(1); return r;
    }
    private IndicadorHospitalario indicador(int idRegistro) {
        IndicadorHospitalario i = new IndicadorHospitalario(); i.setIdIndicador(idRegistro);
        i.setRegistroHospitalario(ventana().get(5)); return i;
    }
    private ModeloPrediccionBinariaResponseDTO respuesta() {
        ModeloPrediccionBinariaResponseDTO d = new ModeloPrediccionBinariaResponseDTO();
        d.setProbabilidadAltaPresion(.41); d.setThreshold(.36); d.setAlertaAltaPresion(true);
        d.setNivelRiesgo(NivelRiesgoPresentacion.ALTO); d.setDescripcionRiesgo("Alta presion");
        d.setUmbralBajoMedio(.20); d.setCodigoIpress("00005617"); d.setServicioHospitalizacion("MEDICINA");
        d.setPeriodoActual("2025-06"); d.setPeriodoPredicho("2025-07");
        d.setHorizontePrediccion("mes_siguiente"); d.setModelo("modelo_ipress_binario_v1"); return d;
    }
    private PrediccionBinariaAutomaticaException assertCodigo(String c, org.junit.jupiter.api.function.Executable x) {
        PrediccionBinariaAutomaticaException e = assertThrows(PrediccionBinariaAutomaticaException.class, x);
        assertEquals(c, e.getCodigo()); return e;
    }
}
