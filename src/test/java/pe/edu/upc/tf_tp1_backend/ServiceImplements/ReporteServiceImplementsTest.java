package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Reporte;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IReporteRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplementsTest {

    @Mock
    private IReporteRepository reporteRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IPrediccionRiesgoRepository prediccionRepository;

    @InjectMocks
    private ReporteServiceImplements service;

    @Test
    void listadoSoloIncluyeReportesDeLaIpressAsignada() {

        Usuario usuario = crearUsuario(1L);
        Reporte reporteIpressA = crearReporte(10, 1L);
        Reporte reporteIpressB = crearReporte(20, 2L);

        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(reporteRepository.findAll()).thenReturn(List.of(reporteIpressA, reporteIpressB));

        List<ReporteListDTO> resultado = service.list(usuario.getCorreo());

        assertEquals(1, resultado.size());
        assertEquals(10, resultado.get(0).getIdReporte());
    }

    @Test
    void reporteDeOtraIpressNoSeExponePorId() {

        Usuario usuario = crearUsuario(1L);
        Reporte reporteAjeno = crearReporte(20, 2L);

        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(reporteRepository.findById(20)).thenReturn(Optional.of(reporteAjeno));

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.listId(usuario.getCorreo(), 20)
        );

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatusCode());
    }

    private Usuario crearUsuario(Long idIpress) {

        Rol rol = new Rol();
        rol.setNombreRol("ATENCION_HOSPITALIZACION");

        Ipress ipress = new Ipress();
        ipress.setIdIpress(idIpress);

        Usuario usuario = new Usuario();
        usuario.setCorreo("hospital@correo.pe");
        usuario.setRol(rol);
        usuario.setIpress(ipress);

        return usuario;
    }

    private Reporte crearReporte(Integer idReporte, Long idIpress) {

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

        Reporte reporte = new Reporte();
        reporte.setIdReporte(idReporte);
        reporte.setPrediccionRiesgo(prediccion);

        return reporte;
    }
}
