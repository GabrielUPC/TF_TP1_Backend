package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionBinariaResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionRequestDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionBinariaAutomaticaRequestDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;
import pe.edu.upc.tf_tp1_backend.Enums.NivelRiesgoPresentacion;
import pe.edu.upc.tf_tp1_backend.Repositories.CalidadInferenciaRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IIpressRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PrediccionBinariaAutomaticaServiceTest {
    private IIpressRepository ipressRepository;
    private IRegistroHospitalarioRepository registroRepository;
    private CalidadInferenciaRepository calidadRepository;
    private ModeloPredictivoClientService modeloClient;
    private PrediccionBinariaAutomaticaService servicio;
    private Ipress ipress;

    @BeforeEach
    void preparar() {
        ipressRepository = mock(IIpressRepository.class);
        registroRepository = mock(IRegistroHospitalarioRepository.class);
        calidadRepository = mock(CalidadInferenciaRepository.class);
        modeloClient = mock(ModeloPredictivoClientService.class);
        servicio = new PrediccionBinariaAutomaticaService(
                ipressRepository, registroRepository, calidadRepository, modeloClient);
        ipress = new Ipress(10L, "00005617", "IPRESS PRUEBA", "II-2", "150101",
                "LIMA", "LIMA", "LIMA");
        when(ipressRepository.findByCodigoRenipressIgnoreCase("00005617"))
                .thenReturn(Optional.of(ipress));
        when(calidadRepository.existePendiente(anyLong(), anyString(), any(), any()))
                .thenReturn(false);
        when(modeloClient.predecirBinario(any())).thenReturn(respuestaValida());
    }

    @Test
    void seisPeriodosConsecutivosValidosGeneranInferencia() {
        prepararVentana(ventanaCompleta());

        ModeloPrediccionBinariaResponseDTO respuesta = servicio.predecir(solicitud());

        assertEquals(0.41, respuesta.getProbabilidadAltaPresion());
        assertEquals(NivelRiesgoPresentacion.ALTO, respuesta.getNivelRiesgo());
        assertEquals("2025-07", respuesta.getPeriodoPredicho());
        verify(modeloClient).predecirBinario(any());
    }

    @Test
    void cincoPeriodosDevuelvenHistorialInsuficiente() {
        List<RegistroHospitalario> registros = ventanaCompleta();
        registros.remove(0);
        prepararVentana(registros);

        assertCodigo("HISTORIAL_INSUFICIENTE", () -> servicio.predecir(solicitud()));
        verifyNoInteractions(modeloClient);
    }

    @Test
    void mesIntermedioAusenteSeDistingue() {
        List<RegistroHospitalario> registros = ventanaCompleta();
        registros.remove(2);
        prepararVentana(registros);

        PrediccionBinariaAutomaticaException error = assertCodigo(
                "MES_INTERMEDIO_FALTANTE", () -> servicio.predecir(solicitud()));
        assertEquals(List.of("2025-03"), error.getPeriodosFaltantes());
    }

    @Test
    void ipressInexistenteNoConsultaRegistros() {
        when(ipressRepository.findByCodigoRenipressIgnoreCase("00005617"))
                .thenReturn(Optional.empty());

        assertCodigo("IPRESS_NO_EXISTE", () -> servicio.predecir(solicitud()));
        verifyNoInteractions(registroRepository, modeloClient);
    }

    @Test
    void servicioInexistenteSeDistingue() {
        prepararVentana(List.of());
        when(registroRepository
                .existsByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(10L, "MEDICINA"))
                .thenReturn(false);

        assertCodigo("SERVICIO_NO_EXISTE", () -> servicio.predecir(solicitud()));
        verifyNoInteractions(modeloClient);
    }

    @Test
    void servicioFueraDeFamilias24Y25SeRechaza() {
        List<RegistroHospitalario> registros = ventanaCompleta();
        registros.forEach(r -> r.setIdHospitalizacion("220100"));
        prepararVentana(registros);

        assertCodigo("SERVICIO_FUERA_ALCANCE", () -> servicio.predecir(solicitud()));
        verifyNoInteractions(modeloClient);
    }

    @Test
    void pendienteDeCalidadImpideInferencia() {
        prepararVentana(ventanaCompleta());
        when(calidadRepository.existePendiente(
                eq(10L), eq("MEDICINA"), eq(YearMonth.of(2025, 1)), eq(YearMonth.of(2025, 6))))
                .thenReturn(true);

        assertCodigo("CALIDAD_PENDIENTE", () -> servicio.predecir(solicitud()));
        verifyNoInteractions(modeloClient);
    }

    @Test
    void servicioSoloPendienteNoSeConfundeConServicioInexistente() {
        prepararVentana(List.of());
        when(calidadRepository.existePendiente(
                eq(10L), eq("MEDICINA"), eq(YearMonth.of(2025, 1)), eq(YearMonth.of(2025, 6))))
                .thenReturn(true);

        assertCodigo("CALIDAD_PENDIENTE", () -> servicio.predecir(solicitud()));

        verify(registroRepository, never())
                .existsByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(anyLong(), anyString());
        verifyNoInteractions(modeloClient);
    }

    @Test
    void archivoRechazadoSeTrataComoCalidadInvalida() {
        List<RegistroHospitalario> registros = ventanaCompleta();
        registros.get(1).getArchivoCargado().setEstadoValidacion("RECHAZADO");
        prepararVentana(registros);

        assertCodigo("CALIDAD_INVALIDA", () -> servicio.predecir(solicitud()));
        verifyNoInteractions(modeloClient);
    }

    @Test
    void periodoDuplicadoProduceConflictoSinElegirRegistro() {
        List<RegistroHospitalario> registros = ventanaCompleta();
        registros.add(registro(2025, 3));
        prepararVentana(registros);

        PrediccionBinariaAutomaticaException error = assertCodigo(
                "PERIODO_DUPLICADO", () -> servicio.predecir(solicitud()));
        assertEquals(409, error.getStatus().value());
        verifyNoInteractions(modeloClient);
    }

    @Test
    void ordenDeBaseDeDatosNoAfectaOrdenCronologicoEnviado() {
        List<RegistroHospitalario> registros = ventanaCompleta();
        Collections.reverse(registros);
        prepararVentana(registros);

        servicio.predecir(solicitud());

        ArgumentCaptor<ModeloPrediccionRequestDTO> captor =
                ArgumentCaptor.forClass(ModeloPrediccionRequestDTO.class);
        verify(modeloClient).predecirBinario(captor.capture());
        assertEquals(List.of(1, 2, 3, 4, 5), captor.getValue().getHistorialUltimosMeses()
                .stream().map(d -> d.getMes()).toList());
        assertEquals(6, captor.getValue().getRegistroActual().getMes());
    }

    @Test
    void fastApiNoSeInvocaConPeriodoActualAusente() {
        List<RegistroHospitalario> registros = ventanaCompleta();
        registros.remove(5);
        prepararVentana(registros);

        assertCodigo("HISTORIAL_INSUFICIENTE", () -> servicio.predecir(solicitud()));
        verifyNoInteractions(modeloClient);
    }

    @Test
    void fastApiSeInvocaExactamenteUnaVezConCincoMesesDeHistorial() {
        prepararVentana(ventanaCompleta());

        servicio.predecir(solicitud());

        ArgumentCaptor<ModeloPrediccionRequestDTO> captor =
                ArgumentCaptor.forClass(ModeloPrediccionRequestDTO.class);
        verify(modeloClient, times(1)).predecirBinario(captor.capture());
        assertEquals(5, captor.getValue().getHistorialUltimosMeses().size());
    }

    @Test
    void requestAlModeloNoContieneInformacionPosteriorAT() {
        prepararVentana(ventanaCompleta());

        servicio.predecir(solicitud());

        ArgumentCaptor<ModeloPrediccionRequestDTO> captor =
                ArgumentCaptor.forClass(ModeloPrediccionRequestDTO.class);
        verify(modeloClient).predecirBinario(captor.capture());
        assertTrue(captor.getValue().getHistorialUltimosMeses().stream()
                .allMatch(d -> YearMonth.of(d.getAnio(), d.getMes()).isBefore(YearMonth.of(2025, 6))));
        assertEquals(YearMonth.of(2025, 6), YearMonth.of(
                captor.getValue().getRegistroActual().getAnio(),
                captor.getValue().getRegistroActual().getMes()));
    }

    @Test
    void conservaThreshold036() {
        prepararVentana(ventanaCompleta());
        assertEquals(0.36, servicio.predecir(solicitud()).getThreshold());
    }

    @Test
    void conservaAlertaCoherenteConProbabilidad() {
        prepararVentana(ventanaCompleta());
        ModeloPrediccionBinariaResponseDTO respuesta = servicio.predecir(solicitud());
        assertEquals(respuesta.getProbabilidadAltaPresion() >= respuesta.getThreshold(),
                respuesta.getAlertaAltaPresion());
    }

    @Test
    void periodoSolicitadoSeUsaComoLimiteExactoDeConsulta() {
        prepararVentana(ventanaCompleta());

        servicio.predecir(solicitud());

        verify(registroRepository).buscarVentanaInferencia(10L, "MEDICINA", 2025, 1, 2025, 6);
    }

    private void prepararVentana(List<RegistroHospitalario> registros) {
        when(registroRepository.buscarVentanaInferencia(
                anyLong(), anyString(), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(registros);
    }

    private List<RegistroHospitalario> ventanaCompleta() {
        List<RegistroHospitalario> registros = new ArrayList<>();
        for (int mes = 1; mes <= 6; mes++) registros.add(registro(2025, mes));
        return registros;
    }

    private RegistroHospitalario registro(int anio, int mes) {
        ArchivoCargado archivo = new ArchivoCargado();
        archivo.setIdArchivo((long) mes);
        archivo.setIpress(ipress);
        archivo.setEstadoValidacion("VALIDADO");
        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setIdRegistro(anio * 100 + mes);
        registro.setArchivoCargado(archivo);
        registro.setAnio(anio);
        registro.setMes(mes);
        registro.setServicioHospitalario("MEDICINA");
        registro.setIdHospitalizacion("241800");
        registro.setSector("MINSA");
        registro.setIngresos(20);
        registro.setEgresos(18);
        registro.setEstancias(120);
        registro.setPacientesCama(240);
        registro.setCamasTotales(10);
        registro.setTotalCamasDisponibles(300);
        registro.setFallecidos(1);
        return registro;
    }

    private PrediccionBinariaAutomaticaRequestDTO solicitud() {
        PrediccionBinariaAutomaticaRequestDTO dto = new PrediccionBinariaAutomaticaRequestDTO();
        dto.setCodigoIpress("00005617");
        dto.setServicio("MEDICINA");
        dto.setPeriodoActual("2025-06");
        return dto;
    }

    private ModeloPrediccionBinariaResponseDTO respuestaValida() {
        ModeloPrediccionBinariaResponseDTO dto = new ModeloPrediccionBinariaResponseDTO();
        dto.setProbabilidadAltaPresion(0.41);
        dto.setThreshold(0.36);
        dto.setAlertaAltaPresion(true);
        dto.setNivelRiesgo(NivelRiesgoPresentacion.ALTO);
        dto.setDescripcionRiesgo(NivelRiesgoPresentacion.ALTO.getDescripcion());
        dto.setUmbralBajoMedio(NivelRiesgoPresentacion.UMBRAL_BAJO_MEDIO);
        dto.setCodigoIpress("00005617");
        dto.setServicioHospitalizacion("MEDICINA");
        dto.setPeriodoActual("2025-06");
        dto.setPeriodoPredicho("2025-07");
        dto.setHorizontePrediccion("mes_siguiente");
        dto.setModelo("modelo_ipress_binario_v1");
        return dto;
    }

    private PrediccionBinariaAutomaticaException assertCodigo(
            String codigo,
            org.junit.jupiter.api.function.Executable accion
    ) {
        PrediccionBinariaAutomaticaException error = assertThrows(
                PrediccionBinariaAutomaticaException.class, accion);
        assertEquals(codigo, error.getCodigo());
        return error;
    }
}
