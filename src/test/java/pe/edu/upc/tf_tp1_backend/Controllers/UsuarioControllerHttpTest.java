package pe.edu.upc.tf_tp1_backend.Controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIpressInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IRolInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IUsuarioInterfaces;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerHttpTest {

    private MockMvc mvc;
    private IUsuarioInterfaces usuarios;
    private IRolInterfaces roles;
    private IIpressInterfaces ipress;
    private Authentication autenticacion;

    @BeforeEach
    void preparar() {
        usuarios = mock(IUsuarioInterfaces.class);
        roles = mock(IRolInterfaces.class);
        ipress = mock(IIpressInterfaces.class);
        autenticacion = mock(Authentication.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        Rol admin = new Rol(1L, "ADMINISTRADOR", null);
        Usuario administrador = new Usuario();
        administrador.setIdUsuario(99L);
        administrador.setRol(admin);
        when(autenticacion.getName()).thenReturn("admin@ipress.pe");
        when(usuarios.buscarPorCorreo("admin@ipress.pe"))
                .thenReturn(Optional.of(administrador));

        UsuarioController controller = new UsuarioController();
        ReflectionTestUtils.setField(controller, "usuarioService", usuarios);
        ReflectionTestUtils.setField(controller, "rolService", roles);
        ReflectionTestUtils.setField(controller, "ipressService", ipress);
        ReflectionTestUtils.setField(controller, "passwordEncoder", encoder);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void devuelve409YMensajeLegibleParaCorreoDeOtraIpress() throws Exception {
        Usuario ocupado = new Usuario();
        ocupado.setIdUsuario(12L);
        when(usuarios.buscarPorCorreo("duplicado@ipress.test"))
                .thenReturn(Optional.of(ocupado));

        mvc.perform(post("/usuarios")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud("duplicado@ipress.test")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("El correo institucional ya está registrado"));

        verify(usuarios, never()).add(any());
    }

    @Test
    void aceptaCorreoNuevoConIpressOperativa() throws Exception {
        when(roles.listId(2L)).thenReturn(new Rol(2L, "ADMISION_REGISTROS", null));
        when(ipress.listId(2L)).thenReturn(new Ipress());

        mvc.perform(post("/usuarios")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud("nuevo@ipress.test")))
                .andExpect(status().isOk());

        verify(usuarios).add(any(Usuario.class));
    }

    @Test
    void devuelve409AlEditarConCorreoDeOtroUsuario() throws Exception {
        Usuario editado = new Usuario();
        editado.setIdUsuario(10L);
        Usuario ocupado = new Usuario();
        ocupado.setIdUsuario(12L);
        when(usuarios.listId(10L)).thenReturn(editado);
        when(usuarios.buscarPorCorreo("duplicado@ipress.test"))
                .thenReturn(Optional.of(ocupado));

        mvc.perform(put("/usuarios")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud("duplicado@ipress.test")
                                .replaceFirst("\\{", "{\"idUsuario\":10,")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("El correo institucional ya está registrado"));

        verify(usuarios, never()).modificar(any());
    }

    private String solicitud(String correo) {
        return "{\"nombre\":\"Usuario QA\",\"correo\":\"" + correo
                + "\",\"contrasena\":\"Temporal-2026\",\"estado\":true,"
                + "\"idRol\":2,\"idIpress\":2}";
    }
}
