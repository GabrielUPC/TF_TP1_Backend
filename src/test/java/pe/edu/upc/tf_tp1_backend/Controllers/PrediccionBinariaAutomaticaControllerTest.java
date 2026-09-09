package pe.edu.upc.tf_tp1_backend.Controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionBinariaResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionBinariaAutomaticaErrorDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionBinariaAutomaticaRequestDTO;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;
import pe.edu.upc.tf_tp1_backend.ServiceImplements.PrediccionBinariaAutomaticaService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrediccionBinariaAutomaticaControllerTest {
    @Test
    void permiteConsumoDesdeAngularLocal() {
        CrossOrigin cors = PrediccionBinariaAutomaticaController.class.getAnnotation(CrossOrigin.class);

        assertNotNull(cors);
        assertArrayEquals(new String[]{"http://localhost:4200"}, cors.origins());
    }

    @Test
    void endpointAutomaticoDelegaSolicitudMinima() {
        PrediccionBinariaAutomaticaService servicio = mock(PrediccionBinariaAutomaticaService.class);
        PrediccionBinariaAutomaticaRequestDTO solicitud = new PrediccionBinariaAutomaticaRequestDTO();
        ModeloPrediccionBinariaResponseDTO esperada = new ModeloPrediccionBinariaResponseDTO();
        when(servicio.predecir(solicitud)).thenReturn(esperada);

        ModeloPrediccionBinariaResponseDTO recibida =
                new PrediccionBinariaAutomaticaController(servicio).predecir(solicitud);

        assertSame(esperada, recibida);
        verify(servicio).predecir(solicitud);
    }

    @Test
    void errorControladoConservaCodigoHttpYCodigoDeNegocio() {
        PrediccionBinariaAutomaticaController controller =
                new PrediccionBinariaAutomaticaController(mock(PrediccionBinariaAutomaticaService.class));
        PrediccionBinariaAutomaticaException error = new PrediccionBinariaAutomaticaException(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "MES_INTERMEDIO_FALTANTE",
                "Falta un mes.",
                List.of("2025-03")
        );

        ResponseEntity<PrediccionBinariaAutomaticaErrorDTO> respuesta = controller.manejarError(error);

        assertEquals(422, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertEquals("sin_prediccion", respuesta.getBody().getEstado());
        assertEquals("MES_INTERMEDIO_FALTANTE", respuesta.getBody().getCodigo());
        assertEquals(List.of("2025-03"), respuesta.getBody().getPeriodosFaltantes());
    }
}
