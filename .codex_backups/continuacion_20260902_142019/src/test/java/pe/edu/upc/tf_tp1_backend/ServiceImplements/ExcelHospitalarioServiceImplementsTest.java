package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.ConsolidadorRegistrosHospitalarios;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.ContenidoArchivoHospitalario;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.DetectorFormatoHospitalario;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.FormatoArchivoHospitalario;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.LectorArchivoHospitalario;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.RegistroHospitalarioImportado;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.ResultadoDeteccionFormato;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.ResultadoTransformacionHospitalaria;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.TransformadorD1;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.TransformadorFormatoInterno;
import pe.edu.upc.tf_tp1_backend.DTOS.ResumenCargaExcelDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    @Mock
    private LectorArchivoHospitalario lectorArchivo;
    @Mock
    private DetectorFormatoHospitalario detectorFormato;
    @Mock
    private TransformadorFormatoInterno transformadorInterno;
    @Mock
    private TransformadorD1 transformadorD1;
    @Mock
    private ConsolidadorRegistrosHospitalarios consolidador;

    @InjectMocks
    private ExcelHospitalarioServiceImplements service;

    private Usuario usuario;
    private Ipress ipress;

    @BeforeEach
    void configurarSeguridad() {
        Rol rol = new Rol();
        rol.setNombreRol("ADMISION_REGISTROS");

        ipress = new Ipress();
        ipress.setIdIpress(9L);
        ipress.setCodigoRenipress("00007636");
        ipress.setNombreIpress("IPRESS PRUEBA");
        ipress.setCategoriaIpress("III-1");
        ipress.setCodigoUbigeo("150101");
        ipress.setDepartamento("LIMA");
        ipress.setProvincia("LIMA");
        ipress.setDistrito("LIMA");

        usuario = new Usuario();
        usuario.setIdUsuario(7L);
        usuario.setCorreo("usuario@prueba.pe");
        usuario.setRol(rol);
        usuario.setIpress(ipress);

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCorreo("usuario@prueba.pe"))
                .thenReturn(Optional.of(usuario));
        when(ipressRepository.findById(9L)).thenReturn(Optional.of(ipress));
        when(archivoRepository.save(any(ArchivoCargado.class)))
                .thenAnswer(invocacion -> {
                    ArchivoCargado archivo = invocacion.getArgument(0);
                    archivo.setIdArchivo(12L);
                    return archivo;
                });
    }

    @Test
    void devuelveDetalleParaFormatoNoReconocido() throws Exception {
        MockMultipartFile archivo = archivoCsv();
        ContenidoArchivoHospitalario contenido =
                new ContenidoArchivoHospitalario(
                        List.of("nombre", "valor"),
                        List.of()
                );
        ResultadoDeteccionFormato deteccion = new ResultadoDeteccionFormato(
                FormatoArchivoHospitalario.NO_RECONOCIDO,
                contenido.getColumnas(),
                DetectorFormatoHospitalario.COLUMNAS_FORMATO_INTERNO,
                DetectorFormatoHospitalario.COLUMNAS_DATASET_D1
        );
        when(lectorArchivo.leer(archivo)).thenReturn(contenido);
        when(detectorFormato.detectar(contenido.getColumnas()))
                .thenReturn(deteccion);

        ResumenCargaExcelDTO respuesta =
                service.cargarValidarYProcesarExcel(
                        archivo,
                        7L,
                        9L,
                        "usuario@prueba.pe"
                );

        assertEquals("NO_RECONOCIDO", respuesta.getFormatoDetectado());
        assertEquals(List.of("nombre", "valor"), respuesta.getColumnasEncontradas());
        assertEquals(0, respuesta.getTotalPrediccionesGeneradas());
        assertEquals("ERROR", respuesta.getEstadoProcesamiento());
    }

    @Test
    void procesaD1YGeneraPredicciones() throws Exception {
        MockMultipartFile archivo = archivoCsv();
        ContenidoArchivoHospitalario contenido =
                new ContenidoArchivoHospitalario(
                        DetectorFormatoHospitalario.COLUMNAS_DATASET_D1,
                        List.of()
                );
        ResultadoDeteccionFormato deteccion = new ResultadoDeteccionFormato(
                FormatoArchivoHospitalario.DATASET_D1,
                contenido.getColumnas(),
                DetectorFormatoHospitalario.COLUMNAS_FORMATO_INTERNO,
                DetectorFormatoHospitalario.COLUMNAS_DATASET_D1
        );
        RegistroHospitalarioImportado registro =
                new RegistroHospitalarioImportado();
        registro.setCodigoIpress("00007636");
        registro.setAnio(2026);
        registro.setMes(1);
        registro.setServicioHospitalario("HOSPITALIZACION GENERAL");
        registro.setIdHospitalizacion("241800");
        registro.setIngresos(10);
        registro.setEgresos(8);
        registro.setEstancias(40);
        registro.setPacientesCama(200);
        registro.setCamasTotales(20);
        registro.setTotalCamasDisponibles(620);
        registro.setFallecidos(0);

        ResultadoTransformacionHospitalaria transformacion =
                new ResultadoTransformacionHospitalaria();
        transformacion.incrementarFilasCoincidentesIpress();
        transformacion.getRegistros().add(registro);

        when(lectorArchivo.leer(archivo)).thenReturn(contenido);
        when(detectorFormato.detectar(contenido.getColumnas()))
                .thenReturn(deteccion);
        when(transformadorD1.transformar(contenido, ipress))
                .thenReturn(transformacion);
        when(consolidador.consolidar(
                transformacion.getRegistros(),
                transformacion.getAdvertencias()
        )).thenReturn(List.of(registro));

        ResumenCargaExcelDTO respuesta =
                service.cargarValidarYProcesarExcel(
                        archivo,
                        7L,
                        9L,
                        "usuario@prueba.pe"
                );

        verify(prediccionService).predecirPorArchivo(12L);
        assertEquals("DATASET_D1", respuesta.getFormatoDetectado());
        assertEquals(1, respuesta.getTotalRegistrosValidos());
        assertEquals(1, respuesta.getTotalPrediccionesGeneradas());
    }

    private MockMultipartFile archivoCsv() {
        return new MockMultipartFile(
                "archivo",
                "hospitalario.csv",
                "text/csv",
                "contenido".getBytes()
        );
    }
}
