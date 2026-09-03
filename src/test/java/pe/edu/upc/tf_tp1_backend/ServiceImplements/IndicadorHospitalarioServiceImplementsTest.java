package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IIndicadorHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicadorHospitalarioServiceImplementsTest {

    @Mock
    private IIndicadorHospitalarioRepository indicadorRepository;

    @Mock
    private IRegistroHospitalarioRepository registroRepository;

    @InjectMocks
    private IndicadorHospitalarioServiceImplements service;

    @Test
    void datasetD1UsaTotalCamasDisponiblesComoDenominador() {
        RegistroHospitalario registro = crearRegistro(1);
        registro.setPacientesCama(704);
        registro.setIngresos(143);
        registro.setEgresos(70);
        registro.setEstancias(350);
        registro.setTotalCamasDisponibles(715);
        registro.setCamasDisponiblesHabilitadas(null);
        registro.setCamasTotales(13);

        IndicadorHospitalario indicador = calcularIndicador(registro);

        assertEquals(0.98, indicador.getOcupacionEstimada());
        assertEquals(11.0, indicador.getPresionIngresosCamas());
        assertEquals(5.38, indicador.getRotacionCamas());
        assertEquals(5.0, indicador.getPromedioEstancia());
    }

    @Test
    void formatoInternoConvierteCamasHabilitadasADiasCamaSoloParaOcupacion() {
        RegistroHospitalario registro = crearRegistro(2);
        registro.setPacientesCama(68);
        registro.setIngresos(17);
        registro.setEgresos(34);
        registro.setEstancias(170);
        registro.setTotalCamasDisponibles(null);
        registro.setCamasDisponiblesHabilitadas(85);
        registro.setCamasTotales(100);

        IndicadorHospitalario indicador = calcularIndicador(registro);

        assertEquals(0.03, indicador.getOcupacionEstimada());
        assertEquals(0.17, indicador.getPresionIngresosCamas());
        assertEquals(0.34, indicador.getRotacionCamas());
        assertEquals(5.0, indicador.getPromedioEstancia());
    }

    @Test
    void diasCamaNoSeConfundenConCamasFisicas() {
        RegistroHospitalario registro = crearRegistro(3);
        registro.setPacientesCama(240);
        registro.setTotalCamasDisponibles(300);
        registro.setCamasTotales(10);
        registro.setIngresos(20);
        registro.setEgresos(5);
        registro.setEstancias(25);
        IndicadorHospitalario indicador = calcularIndicador(registro);
        assertEquals(0.8, indicador.getOcupacionEstimada());
        assertEquals(2.0, indicador.getPresionIngresosCamas());
        assertEquals(0.5, indicador.getRotacionCamas());
        assertEquals(5.0, indicador.getPromedioEstancia());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void denominadoresInvalidosSonSeguros(Integer denominador) {
        RegistroHospitalario registro = crearRegistro(4);
        registro.setPacientesCama(240);
        registro.setIngresos(10);
        registro.setEstancias(25);
        registro.setTotalCamasDisponibles(denominador);
        registro.setCamasTotales(denominador);
        registro.setEgresos(denominador);
        IndicadorHospitalario indicador = calcularIndicador(registro);
        assertEquals(0.0, indicador.getOcupacionEstimada());
        assertEquals(0.0, indicador.getPresionIngresosCamas());
        assertEquals(0.0, indicador.getRotacionCamas());
        assertEquals(0.0, indicador.getPromedioEstancia());
    }

    @Test
    void diasCamaCeroNoSeSustituyenPorCamasHabilitadas() {
        RegistroHospitalario registro = crearRegistro(5);
        registro.setTotalCamasDisponibles(0);
        registro.setCamasDisponiblesHabilitadas(10);
        registro.setPacientesCama(240);
        assertEquals(0.0, calcularIndicador(registro).getOcupacionEstimada());
    }

    private RegistroHospitalario crearRegistro(Integer idRegistro) {
        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setIdRegistro(idRegistro);
        registro.setAnio(2026);
        registro.setMes(1);
        return registro;
    }

    private IndicadorHospitalario calcularIndicador(RegistroHospitalario registro) {
        when(registroRepository.findById(registro.getIdRegistro()))
                .thenReturn(Optional.of(registro));
        when(indicadorRepository.findByRegistroHospitalario_IdRegistro(
                registro.getIdRegistro()
        )).thenReturn(Optional.empty());
        when(indicadorRepository.save(any(IndicadorHospitalario.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        service.calcularPorRegistro(registro.getIdRegistro());

        ArgumentCaptor<IndicadorHospitalario> captor =
                ArgumentCaptor.forClass(IndicadorHospitalario.class);
        verify(indicadorRepository).save(captor.capture());
        return captor.getValue();
    }
}
