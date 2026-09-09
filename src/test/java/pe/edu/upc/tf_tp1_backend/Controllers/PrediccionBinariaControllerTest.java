package pe.edu.upc.tf_tp1_backend.Controllers;

import org.junit.jupiter.api.Test;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionBinariaResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionRequestDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionBinariaAutomaticaErrorDTO;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;
import pe.edu.upc.tf_tp1_backend.ServiceImplements.ModeloPredictivoClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrediccionBinariaControllerTest {

    @Test
    void delegaEnClienteBinarioSinTransformarRespuesta() {
        ModeloPredictivoClientService cliente = mock(ModeloPredictivoClientService.class);
        ModeloPrediccionRequestDTO solicitud = new ModeloPrediccionRequestDTO();
        ModeloPrediccionBinariaResponseDTO esperada = new ModeloPrediccionBinariaResponseDTO();
        esperada.setProbabilidadAltaPresion(0.78);
        esperada.setThreshold(0.36);
        esperada.setAlertaAltaPresion(true);
        when(cliente.predecirBinario(solicitud)).thenReturn(esperada);

        ModeloPrediccionBinariaResponseDTO recibida =
                new PrediccionBinariaController(cliente).predecirBinario(solicitud);

        assertSame(esperada, recibida);
    }

    @Test
    void endpointTecnicoEstaRestringidoAAdministradores() throws Exception {
        PreAuthorize regla = PrediccionBinariaController.class
                .getDeclaredMethod("predecirBinario", ModeloPrediccionRequestDTO.class)
                .getAnnotation(PreAuthorize.class);
        assertNotNull(regla);
        assertEquals("hasRole('ADMINISTRADOR')", regla.value());
    }

    @Test
    void exponeCodigoControladoSiModeloRespondeInconsistente() {
        PrediccionBinariaController controller =
                new PrediccionBinariaController(mock(ModeloPredictivoClientService.class));
        PrediccionBinariaAutomaticaException error = new PrediccionBinariaAutomaticaException(
                HttpStatus.BAD_GATEWAY,
                "RESPUESTA_MODELO_INCONSISTENTE",
                "Respuesta inconsistente."
        );

        ResponseEntity<PrediccionBinariaAutomaticaErrorDTO> respuesta =
                controller.manejarRespuestaInconsistente(error);

        assertEquals(502, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertEquals("RESPUESTA_MODELO_INCONSISTENTE", respuesta.getBody().getCodigo());
    }
}
