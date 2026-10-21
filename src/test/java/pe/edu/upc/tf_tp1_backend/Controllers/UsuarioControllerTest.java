package pe.edu.upc.tf_tp1_backend.Controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.UsuarioDTO;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIpressInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IRolInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IUsuarioInterfaces;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock private IUsuarioInterfaces usuarioService;
    @Mock private IRolInterfaces rolService;
    @Mock private IIpressInterfaces ipressService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Authentication authentication;
    @InjectMocks private UsuarioController controller;

    @BeforeEach
    void autenticarAdministrador() {
        Rol rolAdministrador = rol("ADMINISTRADOR");
        Usuario administrador = new Usuario();
        administrador.setIdUsuario(99L);
        administrador.setRol(rolAdministrador);
        when(authentication.getName()).thenReturn("admin@ipress.pe");
        when(usuarioService.buscarPorCorreo("admin@ipress.pe"))
                .thenReturn(Optional.of(administrador));
    }

    @Test
    void rechazaCrearUsuarioDeAdmisionSinIpress() {
        UsuarioDTO dto = solicitud(2L, null);
        when(rolService.listId(2L)).thenReturn(rol("ADMISION_REGISTROS"));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controller.registrar(dto, authentication));

        assertEquals(400, error.getStatusCode().value());
        assertEquals("Seleccione una IPRESS para el rol operativo asignado", error.getReason());
        verify(usuarioService, never()).add(any());
    }

    @Test
    void rechazaModificarUsuarioDeHospitalizacionSinIpress() {
        UsuarioDTO dto = solicitud(3L, null);
        dto.setIdUsuario(10L);
        Usuario existente = new Usuario();
        existente.setIdUsuario(10L);
        when(usuarioService.listId(10L)).thenReturn(existente);
        when(rolService.listId(3L)).thenReturn(rol("ATENCION_HOSPITALIZACION"));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> controller.modificar(dto, authentication));

        assertEquals(400, error.getStatusCode().value());
        verify(usuarioService, never()).modificar(any());
    }

    @Test
    void permiteUsuarioOperativoConIpressValida() {
        UsuarioDTO dto = solicitud(3L, 1L);
        when(rolService.listId(3L)).thenReturn(rol("ATENCION_HOSPITALIZACION"));
        when(ipressService.listId(1L)).thenReturn(new Ipress());

        controller.registrar(dto, authentication);

        verify(usuarioService).add(any(Usuario.class));
    }

    @Test
    void administradorNoRequiereIpress() {
        UsuarioDTO dto = solicitud(1L, null);
        when(rolService.listId(1L)).thenReturn(rol("ADMINISTRADOR"));

        controller.registrar(dto, authentication);

        verify(usuarioService).add(any(Usuario.class));
    }

    private UsuarioDTO solicitud(Long idRol, Long idIpress) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNombre("Usuario QA");
        dto.setCorreo("qa@ipress.pe");
        dto.setContrasena("Temporal-2026");
        dto.setEstado(true);
        dto.setIdRol(idRol);
        dto.setIdIpress(idIpress);
        return dto;
    }

    private Rol rol(String nombre) {
        Rol rol = new Rol();
        rol.setNombreRol(nombre);
        return rol;
    }
}
