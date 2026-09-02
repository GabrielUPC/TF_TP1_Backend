package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.charset.StandardCharsets;
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
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.TratamientoCalidadDatosHospitalarios;
import pe.edu.upc.tf_tp1_backend.Repositories.IHallazgoCalidadDatosRepository;
import pe.edu.upc.tf_tp1_backend.DTOS.ResumenCargaExcelDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IIpressRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroPendienteCalidadDatosRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIndicadorHospitalarioInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

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
    @Spy
    private TratamientoCalidadDatosHospitalarios tratamientoCalidad = new TratamientoCalidadDatosHospitalarios();
    @Mock
    private IHallazgoCalidadDatosRepository hallazgoCalidadRepository;
    @Mock
    private IRegistroPendienteCalidadDatosRepository registroPendienteRepository;

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

    @Test
    void apartaGrupoCompletoAntesDeConsolidarYProcesaLosDemas() throws Exception {
        MockMultipartFile archivo = archivoCsv();
        ContenidoArchivoHospitalario contenido = new ContenidoArchivoHospitalario(
                DetectorFormatoHospitalario.COLUMNAS_DATASET_D1,
                List.of()
        );
        ResultadoDeteccionFormato deteccion = new ResultadoDeteccionFormato(
                FormatoArchivoHospitalario.DATASET_D1,
                contenido.getColumnas(),
                DetectorFormatoHospitalario.COLUMNAS_FORMATO_INTERNO,
                DetectorFormatoHospitalario.COLUMNAS_DATASET_D1
        );

        RegistroHospitalarioImportado invalidoEnero = registro(2, 1, " Medicina ", 0, 4, 0, 20);
        RegistroHospitalarioImportado validoMismoGrupo = registro(3, 1, "MEDICINA", 10, 4, 310, 20);
        RegistroHospitalarioImportado validoFebrero = registro(4, 2, "MEDICINA", 10, 4, 280, 20);

        ResultadoTransformacionHospitalaria transformacion = new ResultadoTransformacionHospitalaria();
        transformacion.incrementarFilasCoincidentesIpress();
        transformacion.incrementarFilasCoincidentesIpress();
        transformacion.incrementarFilasCoincidentesIpress();
        transformacion.getRegistros().addAll(List.of(invalidoEnero, validoMismoGrupo, validoFebrero));

        when(lectorArchivo.leer(archivo)).thenReturn(contenido);
        when(detectorFormato.detectar(contenido.getColumnas())).thenReturn(deteccion);
        when(transformadorD1.transformar(contenido, ipress)).thenReturn(transformacion);
        when(consolidador.consolidar(any(), any())).thenReturn(List.of(validoFebrero));

        ResumenCargaExcelDTO respuesta = service.cargarValidarYProcesarExcel(
                archivo, 7L, 9L, "usuario@prueba.pe"
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RegistroHospitalarioImportado>> antesDeConsolidar =
                ArgumentCaptor.forClass(List.class);
        verify(consolidador).consolidar(antesDeConsolidar.capture(), any());
        assertEquals(1, antesDeConsolidar.getValue().size());
        assertSame(validoFebrero, antesDeConsolidar.getValue().get(0));
        verify(prediccionService).predecirPorArchivo(12L);
        assertEquals("PROCESADO_PARCIAL", respuesta.getEstadoProcesamiento());
        assertEquals(1, respuesta.getTotalRegistrosValidos());
        assertEquals(1, respuesta.getTotalGruposPendientes());
        assertEquals(2, respuesta.getPendientesCalidad().size());
        assertEquals(2, respuesta.getTotalRegistrosPendientes());
        assertEquals(List.of("Q05, Q06", "Q05, Q06"), respuesta.getPendientesCalidad().stream()
                .map(p -> p.getRegla()).toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"dias_cama_disponible", "nro_total_camas_disponib"})
    void csvMixtoRealConservaPendientesYGuardaSoloElGrupoValido(String columnaDiasCama) {
        ResumenCargaExcelDTO respuesta = cargarCsvReal(columnaDiasCama, """
                2026,1,00007636,241500,MEDICINA,4,0,0,0,0,0,0
                2026,1,00007636,241500, medicina ,5,4,20,20,10,310,0
                2026,2,00007636,241600,CIRUGIA,2,1,10,20,10,0,0
                2026,3,00007636,241700,PEDIATRIA,8,7,30,25,12,372,0
                """);
        assertEquals("PROCESADO_PARCIAL", respuesta.getEstadoProcesamiento());
        assertEquals(4, respuesta.getTotalFilasLeidas());
        assertEquals(1, respuesta.getTotalRegistrosValidos());
        assertEquals(2, respuesta.getTotalGruposPendientes());
        assertEquals(3, respuesta.getTotalRegistrosPendientes());
        assertEquals(List.of("Q05", "Q05", "Q06"), respuesta.getPendientesCalidad().stream()
                .map(p -> p.getRegla()).toList());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario>> validos =
                ArgumentCaptor.forClass(List.class);
        verify(registroRepository).saveAll(validos.capture());
        assertEquals(1, validos.getValue().size());
        assertEquals("PEDIATRIA", validos.getValue().get(0).getServicioHospitalario());
        assertEquals(372, validos.getValue().get(0).getTotalCamasDisponibles());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<pe.edu.upc.tf_tp1_backend.Entities.RegistroPendienteCalidadDatos>> pendientes =
                ArgumentCaptor.forClass(List.class);
        verify(registroPendienteRepository).saveAll(pendientes.capture());
        assertEquals(3, pendientes.getValue().size());
        org.junit.jupiter.api.Assertions.assertTrue(pendientes.getValue().stream()
                .allMatch(p -> p.getDatosRegistroJson().contains("codigoIpress")));
        verify(prediccionService).predecirPorArchivo(12L);
    }

    @Test
    void csvConTodosLosGruposPendientesNoSolicitaIndicadoresNiPredicciones() {
        ResumenCargaExcelDTO respuesta = cargarCsvReal("dias_cama_disponible", """
                2026,1,00007636,241500,MEDICINA,4,0,0,0,0,0,0
                2026,2,00007636,241600,CIRUGIA,2,1,10,20,10,0,0
                """);
        assertEquals("PENDIENTE_VALIDACION", respuesta.getEstadoValidacion());
        assertEquals("SIN_PREDICCIONES", respuesta.getEstadoProcesamiento());
        assertEquals(0, respuesta.getTotalPrediccionesGeneradas());
        assertEquals(2, respuesta.getTotalGruposPendientes());
        verify(registroRepository, never()).saveAll(any());
        verify(indicadorService, never()).calcularPorArchivo(any());
        verify(prediccionService, never()).predecirPorArchivo(any());
    }

    @Test
    void csvConErrorDeFormatoDeFilaNoSeClasificaComoPendienteQ05Q06() {
        ResumenCargaExcelDTO respuesta = cargarCsvReal("dias_cama_disponible", """
                2026,13,00007636,241500,MEDICINA,4,0,0,0,10,310,0
                """);
        assertEquals("ERROR", respuesta.getEstadoValidacion());
        assertEquals(0, respuesta.getTotalGruposPendientes());
        assertEquals(0, respuesta.getTotalPrediccionesGeneradas());
        verify(prediccionService, never()).predecirPorArchivo(any());
    }

    private ResumenCargaExcelDTO cargarCsvReal(String columnaDiasCama, String filas) {
        // Lectura, columnas, unidades, reglas y consolidacion reales; BD y modelo simulados.
        ReflectionTestUtils.setField(service, "lectorArchivo", new LectorArchivoHospitalario());
        ReflectionTestUtils.setField(service, "detectorFormato", new DetectorFormatoHospitalario());
        ReflectionTestUtils.setField(service, "transformadorD1", new TransformadorD1());
        ReflectionTestUtils.setField(service, "consolidador", new ConsolidadorRegistrosHospitalarios());
        String cabecera = "anio,mes,codigo_ipress,id_hospitalizacion,servicio_hospitalario,"
                + "nro_total_hospit_ing,nro_total_hospit_egr,nro_total_estancias,"
                + "nro_total_pacientes_camas,nro_total_camas," + columnaDiasCama
                + ",nro_total_fallecidos,ubigeo,departamento,provincia,distrito,sector,categoria,razon_soc\n";
        String filasCompletas = filas.lines().filter(linea -> !linea.isBlank())
                .map(linea -> linea + ",150101,LIMA,LIMA,LIMA,MINSA,III-1,IPRESS PRUEBA")
                .collect(java.util.stream.Collectors.joining("\n"));
        MockMultipartFile archivo = new MockMultipartFile("archivo", "nuevo.csv", "text/csv",
                (cabecera + filasCompletas).getBytes(StandardCharsets.UTF_8));
        return service.cargarValidarYProcesarExcel(archivo, 7L, 9L, "usuario@prueba.pe");
    }

    private RegistroHospitalarioImportado registro(
            int fila,
            int mes,
            String servicio,
            int camas,
            int ingresos,
            int diasCamaDisponibles,
            int pacientesDia
    ) {
        RegistroHospitalarioImportado registro = new RegistroHospitalarioImportado();
        registro.setNumeroFila(fila);
        registro.setCodigoIpress("00007636");
        registro.setAnio(2026);
        registro.setMes(mes);
        registro.setServicioHospitalario(servicio);
        registro.setIngresos(ingresos);
        registro.setEgresos(0);
        registro.setEstancias(0);
        registro.setPacientesCama(pacientesDia);
        registro.setCamasTotales(camas);
        registro.setTotalCamasDisponibles(diasCamaDisponibles);
        registro.setFallecidos(0);
        return registro;
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
