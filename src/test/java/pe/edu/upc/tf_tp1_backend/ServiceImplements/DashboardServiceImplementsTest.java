package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardResumenDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplementsTest {

    @Mock
    private IPrediccionRiesgoRepository prediccionRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    private DashboardServiceImplements service;

    @Test
    void resumenSoloIncluyePrediccionesDeLaIpressAsignada() {

        Usuario usuario = crearUsuario("hospital@correo.pe", "ATENCION_HOSPITALIZACION", 1L);
        PrediccionRiesgo prediccionIpressA = crearPrediccion(1L, "ALTO");
        PrediccionRiesgo prediccionIpressB = crearPrediccion(2L, "BAJO");

        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(prediccionRepository.findAll()).thenReturn(List.of(
                prediccionIpressA,
                prediccionIpressB
        ));

        DashboardResumenDTO resultado = service.obtenerResumenGeneral(usuario.getCorreo());

        assertEquals(1L, resultado.getTotalPredicciones());
        assertEquals(1L, resultado.getTotalRiesgoAlto());
        assertEquals(0L, resultado.getTotalRiesgoBajo());
    }

    @Test
    void administradorNoPuedeConsultarDashboard() {

        Usuario usuario = crearUsuario("admin@correo.pe", "ADMINISTRADOR", null);
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.obtenerDetalleGeneral(usuario.getCorreo())
        );

        assertEquals(HttpStatus.FORBIDDEN, excepcion.getStatusCode());
        verify(prediccionRepository, never()).findAll();
    }

    private Usuario crearUsuario(String correo, String nombreRol, Long idIpress) {

        Rol rol = new Rol();
        rol.setNombreRol(nombreRol);

        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setRol(rol);

        if (idIpress != null) {
            Ipress ipress = new Ipress();
            ipress.setIdIpress(idIpress);
            usuario.setIpress(ipress);
        }

        return usuario;
    }

    private PrediccionRiesgo crearPrediccion(Long idIpress, String nivelRiesgo) {

        Ipress ipress = new Ipress();
        ipress.setIdIpress(idIpress);

        ArchivoCargado archivo = new ArchivoCargado();
        archivo.setIpress(ipress);

        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setArchivoCargado(archivo);

        IndicadorHospitalario indicador = new IndicadorHospitalario();
        indicador.setRegistroHospitalario(registro);

        PrediccionRiesgo prediccion = new PrediccionRiesgo();
        prediccion.setIndicadorHospitalario(indicador);
        prediccion.setNivelRiesgo(nivelRiesgo);

        return prediccion;
    }
}
