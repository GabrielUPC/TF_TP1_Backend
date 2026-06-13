package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
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
        assertEquals(0.2, indicador.getPresionIngresosCamas());
        assertEquals(0.1, indicador.getRotacionCamas());
        assertEquals(5.0, indicador.getPromedioEstancia());
    }

    @Test
    void formatoInternoUsaCamasDisponiblesHabilitadasComoDenominador() {
        RegistroHospitalario registro = crearRegistro(2);
        registro.setPacientesCama(68);
        registro.setIngresos(17);
        registro.setEgresos(34);
        registro.setEstancias(170);
        registro.setTotalCamasDisponibles(null);
        registro.setCamasDisponiblesHabilitadas(85);
        registro.setCamasTotales(100);

        IndicadorHospitalario indicador = calcularIndicador(registro);

        assertEquals(0.8, indicador.getOcupacionEstimada());
        assertEquals(0.2, indicador.getPresionIngresosCamas());
        assertEquals(0.4, indicador.getRotacionCamas());
        assertEquals(5.0, indicador.getPromedioEstancia());
    }

    private RegistroHospitalario crearRegistro(Integer idRegistro) {
        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setIdRegistro(idRegistro);
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
