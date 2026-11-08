package pe.edu.upc.tf_tp1_backend.Controllers;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.upc.tf_tp1_backend.DTOS.IndicadorHospitalarioListDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIndicadorHospitalarioInterfaces;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IndicadorHospitalarioControllerTest {

    @Test
    void archivoSerializaPromedioEstanciaCeroComoNumero() throws Exception {
        IIndicadorHospitalarioInterfaces service = mock(IIndicadorHospitalarioInterfaces.class);
        IndicadorHospitalarioListDTO indicador = new IndicadorHospitalarioListDTO();
        indicador.setPromedioEstancia(0.0);
        when(service.listByArchivo(38L)).thenReturn(List.of(indicador));

        IndicadorHospitalarioController controller = new IndicadorHospitalarioController();
        ReflectionTestUtils.setField(controller, "iS", service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(get("/indicadores-hospitalarios/archivo/38"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].promedioEstancia").isNumber())
                .andExpect(jsonPath("$[0].promedioEstancia").value(0.0));
    }
}
