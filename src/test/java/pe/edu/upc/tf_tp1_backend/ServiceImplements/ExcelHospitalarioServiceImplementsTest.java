package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ResumenCargaExcelDTO;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IIpressRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIndicadorHospitalarioInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelHospitalarioServiceImplementsTest {

    @Mock
    private IArchivoCargadoRepository archivoRepository;

    @Mock
    private IReporteInterfaces reporteService;

    @Mock
    private IRegistroHospitalarioRepository registroRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IIpressRepository ipressRepository;

    @Mock
    private IIndicadorHospitalarioInterfaces indicadorService;

    @Mock
    private IPrediccionRiesgoInterfaces prediccionService;

    @InjectMocks
    private ExcelHospitalarioServiceImplements service;

    @Test
    void rechazaCargaCuandoSeManipulaLaIpress() {

        Usuario usuario = crearUsuario(5L, 10L);
        Ipress ipressSolicitada = crearIpress(20L, "00009999");
        MockMultipartFile archivo = crearArchivo(service.generarPlantillaExcel());

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(ipressRepository.findById(20L)).thenReturn(Optional.of(ipressSolicitada));
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.cargarValidarYProcesarExcel(
                        archivo,
                        5L,
                        20L,
                        usuario.getCorreo()
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, excepcion.getStatusCode());
        verify(archivoRepository, never()).save(any());
    }

    @Test
    void permiteCargaCuandoUsuarioRolEIpressCoinciden() {

        Usuario usuario = crearUsuario(5L, 10L);
        Ipress ipress = usuario.getIpress();
        MockMultipartFile archivo = crearArchivo(service.generarPlantillaExcel());

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(ipressRepository.findById(10L)).thenReturn(Optional.of(ipress));
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(archivoRepository.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        ResumenCargaExcelDTO resultado = service.cargarValidarYProcesarExcel(
                archivo,
                5L,
                10L,
                usuario.getCorreo()
        );

        assertEquals("PROCESADO", resultado.getEstadoProcesamiento());
        assertEquals(1, resultado.getRegistrosValidos());
        verify(registroRepository).saveAll(any());
        verify(reporteService).generarPorArchivo(null, 5L);
    }

    private Usuario crearUsuario(Long idUsuario, Long idIpress) {

        Rol rol = new Rol();
        rol.setNombreRol("ADMISION_REGISTROS");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre("Usuario Admision");
        usuario.setCorreo("admision@correo.pe");
        usuario.setRol(rol);
        usuario.setIpress(crearIpress(idIpress, "00001234"));

        return usuario;
    }

    private Ipress crearIpress(Long idIpress, String codigoRenipress) {

        Ipress ipress = new Ipress();
        ipress.setIdIpress(idIpress);
        ipress.setCodigoRenipress(codigoRenipress);

        return ipress;
    }

    private MockMultipartFile crearArchivo(byte[] contenido) {
        return new MockMultipartFile(
                "archivo",
                "hospitalizacion.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                contenido
        );
    }
}
