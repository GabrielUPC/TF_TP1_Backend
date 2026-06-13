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
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IIndicadorHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals("XGBoost - FastAPI", guardada.getModeloUtilizado());
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
