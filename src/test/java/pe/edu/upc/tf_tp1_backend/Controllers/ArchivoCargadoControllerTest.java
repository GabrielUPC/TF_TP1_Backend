package pe.edu.upc.tf_tp1_backend.Controllers;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IArchivoCargadoInterfaces;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchivoCargadoControllerTest {

    @Test
    void historialSerializaInstanteUtcYDejaHistoricoSinZonaAtribuida() throws Exception {
        ArchivoCargadoController controller = new ArchivoCargadoController();
        IArchivoCargadoInterfaces service = mock(IArchivoCargadoInterfaces.class);
        Authentication authentication = mock(Authentication.class);
        ReflectionTestUtils.setField(controller, "archivoService", service);
        when(authentication.getName()).thenReturn("admision@correo.pe");

        ArchivoCargado nuevo = new ArchivoCargado();
        nuevo.setIdArchivo(39L);
        nuevo.setFechaCarga(LocalDateTime.of(2026, 11, 1, 19, 37));
        nuevo.setFechaCargaUtc(Instant.parse("2026-11-01T19:37:00Z"));
        ArchivoCargado historico = new ArchivoCargado();
        historico.setIdArchivo(38L);
        historico.setFechaCarga(LocalDateTime.of(2026, 11, 1, 19, 37));
        when(service.listarPorUsuarioAutenticado("admision@correo.pe"))
                .thenReturn(List.of(nuevo, historico));

        var respuesta = controller.listar(authentication);
        assertEquals("2026-11-01T19:37:00Z", respuesta.get(0).getFechaCargaUtc());
        assertEquals(null, respuesta.get(1).getFechaCargaUtc());
        String json = new ObjectMapper().writeValueAsString(respuesta);
        assertTrue(json.contains("\"fechaCargaUtc\":\"2026-11-01T19:37:00Z\""));
    }
}
