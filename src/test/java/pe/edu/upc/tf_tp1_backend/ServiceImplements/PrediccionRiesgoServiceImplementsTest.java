package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloDatosHospitalariosDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionRequestDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionRiesgoListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IIndicadorHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IHallazgoCalidadDatosRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrediccionRiesgoServiceImplementsTest {

    @Mock
    private IPrediccionRiesgoRepository prediccionRepository;

    @Mock
    private IIndicadorHospitalarioRepository indicadorRepository;

    @Mock
    private IRegistroHospitalarioRepository registroRepository;

    @Mock
    private ModeloPredictivoClientService modeloClient;
    @Mock private IHallazgoCalidadDatosRepository hallazgoCalidadRepository;

    @InjectMocks
    private PrediccionRiesgoServiceImplements service;

    @Test
    void enviaRegistroEHistorialAlModeloYGuardaLaRespuesta() {
        Ipress ipress = crearIpress();
        RegistroHospitalario enero = crearRegistro(1, 2026, 1, ipress);
        RegistroHospitalario febrero = crearRegistro(2, 2026, 2, ipress);
        RegistroHospitalario marzo = crearRegistro(3, 2026, 3, ipress);

        IndicadorHospitalario indicador = new IndicadorHospitalario();
        indicador.setIdIndicador(10);
        indicador.setRegistroHospitalario(marzo);

        ModeloPrediccionResponseDTO respuesta = new ModeloPrediccionResponseDTO();
        respuesta.setNivelRiesgoPredicho("alto");
        respuesta.setProbabilidad(0.87);
        respuesta.setRiesgoInsuficienciaCapacidad(0.91);
        respuesta.setProbabilidadRiesgoBajo(0.03);
        respuesta.setProbabilidadRiesgoMedio(0.10);
        respuesta.setProbabilidadRiesgoAlto(0.87);

        when(indicadorRepository.findById(10)).thenReturn(Optional.of(indicador));
        when(prediccionRepository.findByIndicadorHospitalario_IdIndicador(10))
                .thenReturn(Optional.empty());
        when(registroRepository
                .findByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(
                        99L,
                        "HOSPITALIZACION GENERAL"
                ))
                .thenReturn(List.of(marzo, enero, febrero));
        when(modeloClient.predecir(any(ModeloPrediccionRequestDTO.class)))
                .thenReturn(respuesta);

        service.predecirPorIndicador(10);

        ArgumentCaptor<ModeloPrediccionRequestDTO> solicitudCaptor =
                ArgumentCaptor.forClass(ModeloPrediccionRequestDTO.class);
        verify(modeloClient).predecir(solicitudCaptor.capture());

        ModeloPrediccionRequestDTO solicitud = solicitudCaptor.getValue();
        assertEquals(3, solicitud.getRegistroActual().getMes());
        assertEquals(2, solicitud.getHistorialUltimosMeses().size());
        assertEquals(1, solicitud.getHistorialUltimosMeses().get(0).getMes());
        assertEquals(2, solicitud.getHistorialUltimosMeses().get(1).getMes());

        ModeloDatosHospitalariosDTO datosActuales = solicitud.getRegistroActual();
        assertEquals("00006207", datosActuales.getCodigoIpress());
        assertEquals("MINSA", datosActuales.getSector());
        assertEquals(2790.0, datosActuales.getTotalCamasDisponibles());
        assertEquals(0.0, datosActuales.getTotalFallecidos());
        assertEquals(
                2790.0,
                solicitud.getHistorialUltimosMeses().get(0).getTotalCamasDisponibles()
        );
        assertEquals(
                2520.0,
                solicitud.getHistorialUltimosMeses().get(1).getTotalCamasDisponibles()
        );

        ArgumentCaptor<PrediccionRiesgo> prediccionCaptor =
                ArgumentCaptor.forClass(PrediccionRiesgo.class);
        verify(prediccionRepository).save(prediccionCaptor.capture());

        PrediccionRiesgo guardada = prediccionCaptor.getValue();
        assertEquals("ALTO", guardada.getNivelRiesgo());
        assertEquals(0.87, guardada.getProbabilidad());
        assertEquals(0.03, guardada.getProbabilidadRiesgoBajo());
        assertEquals(0.10, guardada.getProbabilidadRiesgoMedio());
        assertEquals(0.87, guardada.getProbabilidadRiesgoAlto());
        assertEquals(0.91, guardada.getRiesgoInsuficienciaCapacidad());
        assertEquals("XGBoost - FastAPI", guardada.getModeloUtilizado());
    }

    @Test
    void datasetD1EnviaCamasDiaSinMultiplicar() {
        Ipress ipress = crearIpress();
        RegistroHospitalario registro = crearRegistro(4, 2026, 1, ipress);
        registro.setCamasDisponiblesHabilitadas(null);
        registro.setTotalCamasDisponibles(715);
        registro.setFallecidos(2);
        registro.setSector("PRIVADO");
        registro.setIdHospitalizacion("241800");

        IndicadorHospitalario indicador = new IndicadorHospitalario();
        indicador.setIdIndicador(11);
        indicador.setRegistroHospitalario(registro);

        ModeloPrediccionResponseDTO respuesta = new ModeloPrediccionResponseDTO();
        respuesta.setNivelRiesgoPredicho("medio");
        respuesta.setProbabilidad(0.72);
        respuesta.setRiesgoInsuficienciaCapacidad(0.56);
        respuesta.setProbabilidadRiesgoBajo(0.12);
        respuesta.setProbabilidadRiesgoMedio(0.72);
        respuesta.setProbabilidadRiesgoAlto(0.16);

        when(indicadorRepository.findById(11)).thenReturn(Optional.of(indicador));
        when(prediccionRepository.findByIndicadorHospitalario_IdIndicador(11))
                .thenReturn(Optional.empty());
        when(registroRepository
                .findByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(
                        99L,
                        "HOSPITALIZACION GENERAL"
                ))
                .thenReturn(List.of(registro));
        when(modeloClient.predecir(any(ModeloPrediccionRequestDTO.class)))
                .thenReturn(respuesta);

        service.predecirPorIndicador(11);

        ArgumentCaptor<ModeloPrediccionRequestDTO> solicitudCaptor =
                ArgumentCaptor.forClass(ModeloPrediccionRequestDTO.class);
        verify(modeloClient).predecir(solicitudCaptor.capture());

        ModeloDatosHospitalariosDTO datos =
                solicitudCaptor.getValue().getRegistroActual();
        assertEquals(715.0, datos.getTotalCamasDisponibles());
        assertEquals(2.0, datos.getTotalFallecidos());
        assertEquals("PRIVADO", datos.getSector());
        assertEquals("241800", datos.getIdHospitalizacion());

        ArgumentCaptor<PrediccionRiesgo> prediccionCaptor =
                ArgumentCaptor.forClass(PrediccionRiesgo.class);
        verify(prediccionRepository).save(prediccionCaptor.capture());
        assertEquals(
                "XGBoost - FastAPI",
                prediccionCaptor.getValue().getModeloUtilizado()
        );
    }

    @Test
    void exponeArchivoIpressYPeriodoPredichoConCambioDeAnio() {
        Ipress ipress = crearIpress();
        RegistroHospitalario diciembre = crearRegistro(20, 2016, 12, ipress);
        diciembre.getArchivoCargado().setIdArchivo(5L);
        diciembre.getArchivoCargado().setNombreArchivo(
                "ConsultaD1_2016.csv"
        );

        IndicadorHospitalario indicador = new IndicadorHospitalario();
        indicador.setIdIndicador(30);
        indicador.setRegistroHospitalario(diciembre);

        PrediccionRiesgo prediccion = new PrediccionRiesgo();
        prediccion.setIdPrediccion(40);
        prediccion.setIndicadorHospitalario(indicador);
        prediccion.setNivelRiesgo("ALTO");
        prediccion.setProbabilidad(0.9);
        prediccion.setModeloUtilizado("XGBoost - FastAPI");

        when(prediccionRepository
                .findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(
                        5L
                ))
                .thenReturn(List.of(prediccion));

        PrediccionRiesgoListDTO dto = service.listByArchivo(5L).get(0);

        assertEquals(5L, dto.getIdArchivo());
        assertEquals("ConsultaD1_2016.csv", dto.getNombreArchivo());
        assertEquals("00006207", dto.getCodigoIpress());
        assertEquals(2016, dto.getAnio());
        assertEquals(12, dto.getMes());
        assertEquals(2017, dto.getAnioPredicho());
        assertEquals(1, dto.getMesPredicho());
        assertEquals("HOSPITALIZACION GENERAL", dto.getServicioHospitalario());
    }

    @Test
    void invalidaPrediccionActualYLaQueApuntaAlMesPendienteSinBorrarlas() {
        Ipress ipress=crearIpress();
        RegistroHospitalario enero=crearRegistro(50,2026,1,ipress);
        RegistroHospitalario febrero=crearRegistro(51,2026,2,ipress);
        PrediccionRiesgo pEnero=prediccion(enero); PrediccionRiesgo pFebrero=prediccion(febrero);
        when(prediccionRepository.findAll()).thenReturn(List.of(pEnero,pFebrero));
        service.invalidarPorPendientes(List.of(new pe.edu.upc.tf_tp1_backend.CargaHospitalaria.HallazgoCalidadImportado(
                7,"00006207",2026,2,"HOSPITALIZACION GENERAL","Q05","pendiente")));
        assertEquals(false,pEnero.getVigente()); assertEquals(false,pFebrero.getVigente());
        verify(prediccionRepository).saveAll(List.of(pEnero,pFebrero));
    }

    @Test
    void conservaMedioAunqueBajoSeaMayorYNoFabricaIndice() {
        ModeloPrediccionResponseDTO respuesta = new ModeloPrediccionResponseDTO();
        respuesta.setNivelRiesgoPredicho("medio");
        respuesta.setNivelRiesgoCodificado(1);
        respuesta.setProbabilidad(0.30);
        respuesta.setProbabilidadRiesgoBajo(0.45);
        respuesta.setProbabilidadRiesgoMedio(0.30);
        respuesta.setProbabilidadRiesgoAlto(0.25);
        PrediccionRiesgo guardada = predecirCon(respuesta, List.of());
        assertEquals("MEDIO", guardada.getNivelRiesgo());
        assertEquals(0.30, guardada.getProbabilidad());
        assertEquals(0.45, guardada.getProbabilidadRiesgoBajo());
        assertEquals(0.30, guardada.getProbabilidadRiesgoMedio());
        assertEquals(0.25, guardada.getProbabilidadRiesgoAlto());
        assertNull(guardada.getRiesgoInsuficienciaCapacidad());
    }

    @Test
    void conservaProbabilidadesDelMapaSinNormalizar() {
        ModeloPrediccionResponseDTO respuesta = respuestaMedio();
        respuesta.setProbabilidadesPorClase(java.util.Map.of("bajo", 0.45, "medio", 0.30, "alto", 0.25));
        PrediccionRiesgo guardada = predecirCon(respuesta, List.of());
        assertEquals(0.45, guardada.getProbabilidadRiesgoBajo());
        assertEquals(0.30, guardada.getProbabilidadRiesgoMedio());
        assertEquals(0.25, guardada.getProbabilidadRiesgoAlto());
    }

    @Test
    void historialRespetaGrupoMesesExactosYNoRellenaHuecos() {
        Ipress ipress = crearIpress();
        RegistroHospitalario enero = crearRegistro(1, 2026, 1, ipress);
        RegistroHospitalario otroServicio = crearRegistro(2, 2026, 2, ipress);
        otroServicio.setServicioHospitalario("CIRUGIA");
        Ipress otra = crearIpress(); otra.setIdIpress(100L);
        RegistroHospitalario otraIpress = crearRegistro(3, 2026, 2, otra);
        PrediccionRiesgo guardada = predecirCon(respuestaMedio(), List.of(enero, otroServicio, otraIpress,
                crearRegistro(4, 2026, 4, ipress), crearRegistro(5, 2025, 12, ipress)));
        assertNull(guardada.getProbabilidadRiesgoAlto());
        ArgumentCaptor<ModeloPrediccionRequestDTO> captor = ArgumentCaptor.forClass(ModeloPrediccionRequestDTO.class);
        verify(modeloClient).predecir(captor.capture());
        assertEquals(1, captor.getValue().getHistorialUltimosMeses().size());
        assertEquals(1, captor.getValue().getHistorialUltimosMeses().get(0).getMes());
        assertEquals("00006207", captor.getValue().getHistorialUltimosMeses().get(0).getCodigoIpress());
        assertEquals("HOSPITALIZACION GENERAL", captor.getValue().getHistorialUltimosMeses().get(0).getServicioHospitalizacion());
    }

    private ModeloPrediccionResponseDTO respuestaMedio() {
        ModeloPrediccionResponseDTO r = new ModeloPrediccionResponseDTO();
        r.setNivelRiesgoPredicho("MEDIO"); r.setProbabilidad(0.30); return r;
    }

    private PrediccionRiesgo predecirCon(ModeloPrediccionResponseDTO respuesta, List<RegistroHospitalario> historial) {
        IndicadorHospitalario indicador = new IndicadorHospitalario();
        indicador.setIdIndicador(10);
        indicador.setRegistroHospitalario(crearRegistro(20, 2026, 3, crearIpress()));
        when(indicadorRepository.findById(10)).thenReturn(Optional.of(indicador));
        when(prediccionRepository.findByIndicadorHospitalario_IdIndicador(10)).thenReturn(Optional.empty());
        when(registroRepository.findByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(99L,
                "HOSPITALIZACION GENERAL")).thenReturn(historial);
        when(modeloClient.predecir(any(ModeloPrediccionRequestDTO.class))).thenReturn(respuesta);
        service.predecirPorIndicador(10);
        ArgumentCaptor<PrediccionRiesgo> captor = ArgumentCaptor.forClass(PrediccionRiesgo.class);
        verify(prediccionRepository).save(captor.capture());
        return captor.getValue();
    }

    private PrediccionRiesgo prediccion(RegistroHospitalario r) {
        IndicadorHospitalario i=new IndicadorHospitalario(); i.setRegistroHospitalario(r);
        PrediccionRiesgo p=new PrediccionRiesgo(); p.setIndicadorHospitalario(i); return p;
    }

    private Ipress crearIpress() {
        Ipress ipress = new Ipress();
        ipress.setIdIpress(99L);
        ipress.setCodigoRenipress("00006207");
        ipress.setCategoriaIpress("III-1");
        ipress.setCodigoUbigeo("150101");
        ipress.setDistrito("LIMA");
        ipress.setProvincia("LIMA");
        ipress.setDepartamento("LIMA");
        return ipress;
    }

    private RegistroHospitalario crearRegistro(
            Integer id,
            Integer anio,
            Integer mes,
            Ipress ipress
    ) {
        ArchivoCargado archivo = new ArchivoCargado();
        archivo.setIpress(ipress);

        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setIdRegistro(id);
        registro.setArchivoCargado(archivo);
        registro.setAnio(anio);
        registro.setMes(mes);
        registro.setServicioHospitalario("HOSPITALIZACION GENERAL");
        registro.setIngresos(80 + mes);
        registro.setEgresos(70 + mes);
        registro.setEstancias(350 + mes);
        registro.setPacientesCama(2500 + mes);
        registro.setCamasTotales(100);
        registro.setCamasDisponiblesHabilitadas(90);
        return registro;
    }
}
