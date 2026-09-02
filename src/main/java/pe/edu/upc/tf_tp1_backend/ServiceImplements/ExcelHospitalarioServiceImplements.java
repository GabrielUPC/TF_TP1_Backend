package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
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
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.ResultadoTratamientoCalidad;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.HallazgoCalidadImportado;
import pe.edu.upc.tf_tp1_backend.DTOS.ErrorValidacionDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ResumenCargaExcelDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Entities.HallazgoCalidadDatos;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroPendienteCalidadDatos;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IIpressRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IHallazgoCalidadDatosRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroPendienteCalidadDatosRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IExcelHospitalarioInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIndicadorHospitalarioInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelHospitalarioServiceImplements
        implements IExcelHospitalarioInterfaces {

    private static final String ROL_ADMISION = "ADMISION_REGISTROS";

    @Autowired
    private IArchivoCargadoRepository archivoRepository;

    @Autowired
    private IReporteInterfaces reporteService;

    @Autowired
    private IRegistroHospitalarioRepository registroRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IIpressRepository ipressRepository;

    @Autowired
    private IIndicadorHospitalarioInterfaces indicadorService;

    @Autowired
    private IPrediccionRiesgoInterfaces prediccionService;

    @Autowired
    private LectorArchivoHospitalario lectorArchivo;

    @Autowired
    private DetectorFormatoHospitalario detectorFormato;

    @Autowired
    private TransformadorFormatoInterno transformadorInterno;

    @Autowired
    private TransformadorD1 transformadorD1;

    @Autowired
    private ConsolidadorRegistrosHospitalarios consolidador;
    @Autowired private TratamientoCalidadDatosHospitalarios tratamientoCalidad;
    @Autowired private IHallazgoCalidadDatosRepository hallazgoCalidadRepository;
    @Autowired private IRegistroPendienteCalidadDatosRepository registroPendienteRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] generarPlantillaExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("hospitalizacion");
            Row cabecera = hoja.createRow(0);

            CellStyle estiloCabecera = workbook.createCellStyle();
            Font fuente = workbook.createFont();
            fuente.setBold(true);
            estiloCabecera.setFont(fuente);

            List<String> columnas =
                    DetectorFormatoHospitalario.COLUMNAS_FORMATO_INTERNO;
            for (int indice = 0; indice < columnas.size(); indice++) {
                Cell cell = cabecera.createCell(indice);
                String columna = "codigo_ipress".equals(columnas.get(indice))
                        ? "codigo_renipress"
                        : columnas.get(indice);
                cell.setCellValue(columna);
                cell.setCellStyle(estiloCabecera);
            }

            Row ejemplo = hoja.createRow(1);
            ejemplo.createCell(0).setCellValue("00001234");
            ejemplo.createCell(1).setCellValue(2026);
            ejemplo.createCell(2).setCellValue(1);
            ejemplo.createCell(3).setCellValue("Hospitalizacion Medicina");
            ejemplo.createCell(4).setCellValue(120);
            ejemplo.createCell(5).setCellValue(110);
            ejemplo.createCell(6).setCellValue(550);
            ejemplo.createCell(7).setCellValue(95);
            ejemplo.createCell(8).setCellValue(100);
            ejemplo.createCell(9).setCellValue(85);

            for (int indice = 0; indice < columnas.size(); indice++) {
                hoja.autoSizeColumn(indice);
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            workbook.write(salida);
            return salida.toByteArray();
        } catch (IOException error) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar la plantilla hospitalaria",
                    error
            );
        }
    }

    @Override
    @Transactional
    public ResumenCargaExcelDTO cargarValidarYProcesarExcel(
            MultipartFile archivo,
            Long idUsuario,
            Long idIpress,
            String correoUsuario
    ) {
        validarArchivo(archivo);

        Usuario usuarioSolicitado = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
        Ipress ipress = ipressRepository.findById(idIpress)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "IPRESS no encontrada"
                ));
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado(correoUsuario);
        validarPermisoCarga(usuarioAutenticado, idUsuario, idIpress);

        ArchivoCargado archivoCargado = crearArchivoPendiente(
                archivo,
                usuarioAutenticado,
                ipress
        );

        ContenidoArchivoHospitalario contenido;
        try {
            contenido = lectorArchivo.leer(archivo);
        } catch (IOException | RuntimeException error) {
            return respuestaErrorLectura(archivoCargado, error);
        }

        ResultadoDeteccionFormato deteccion = detectorFormato.detectar(
                contenido.getColumnas()
        );
        archivoCargado.setFormato(deteccion.getFormato().name());
        archivoRepository.save(archivoCargado);

        if (deteccion.getFormato() == FormatoArchivoHospitalario.NO_RECONOCIDO) {
            return respuestaFormatoNoReconocido(
                    archivoCargado,
                    contenido,
                    deteccion
            );
        }

        ResultadoTransformacionHospitalaria transformacion =
                deteccion.getFormato() == FormatoArchivoHospitalario.DATASET_D1
                        ? transformadorD1.transformar(contenido, ipress)
                        : transformadorInterno.transformar(contenido, ipress);

        ResultadoTratamientoCalidad tratamiento = tratamientoCalidad.aplicar(
                transformacion.getRegistros()
        );
        persistirHallazgos(archivoCargado, tratamiento.hallazgos());
        persistirRegistrosPendientes(archivoCargado, tratamiento);
        prediccionService.invalidarPorPendientes(tratamiento.hallazgos());
        if (tratamiento.gruposPendientes() > 0) {
            transformacion.getAdvertencias().add(
                    tratamiento.gruposPendientes() + " grupos quedaron sin prediccion: datos pendientes de validacion Q05/Q06."
            );
        }
        List<RegistroHospitalarioImportado> consolidados =
                consolidador.consolidar(
                        tratamiento.registrosValidos(),
                        transformacion.getAdvertencias()
                );
        transformacion.setRegistros(consolidados);

        if (transformacion.getFilasCoincidentesIpress() == 0) {
            return respuestaSinRegistros(
                    archivoCargado,
                    contenido,
                    deteccion,
                    transformacion,
                    "No se encontraron registros validos para la IPRESS asignada al usuario."
            );
        }

        if (consolidados.isEmpty()) {
            if (tratamiento.gruposPendientes() == 0) {
                return respuestaSinRegistros(
                        archivoCargado, contenido, deteccion, transformacion,
                        "No se encontraron registros validos para procesar."
                );
            }
            archivoCargado.setEstadoValidacion("PENDIENTE_VALIDACION");
            archivoCargado.setEstadoProcesamiento("SIN_PREDICCIONES");
            archivoRepository.save(archivoCargado);
            ResumenCargaExcelDTO sinValidos = construirResumen(
                    archivoCargado,
                    contenido,
                    deteccion,
                    transformacion,
                    0,
                    0,
                    "Sin prediccion: datos pendientes de validacion."
            );
            return agregarPendientes(sinValidos, tratamiento);
        }

        completarDatosIpress(ipress, consolidados.get(0));
        ipressRepository.save(ipress);

        List<RegistroHospitalario> registros = consolidados.stream()
                .map(registro -> convertirEntidad(registro, archivoCargado))
                .toList();
        registroRepository.saveAll(registros);

        indicadorService.calcularPorArchivo(archivoCargado.getIdArchivo());
        prediccionService.predecirPorArchivo(archivoCargado.getIdArchivo());
        reporteService.generarPorArchivo(
                archivoCargado.getIdArchivo(),
                usuarioSolicitado.getIdUsuario()
        );

        boolean parcial = tratamiento.gruposPendientes() > 0;
        archivoCargado.setEstadoValidacion(parcial ? "VALIDADO_CON_PENDIENTES" : "VALIDADO");
        archivoCargado.setEstadoProcesamiento(parcial ? "PROCESADO_PARCIAL" : "PROCESADO");
        archivoRepository.save(archivoCargado);

        String mensaje = "Archivo procesado correctamente. Formato detectado: "
                + deteccion.getFormato().name()
                + ". Registros validos: " + registros.size()
                + ". Predicciones generadas: " + registros.size() + ".";

        ResumenCargaExcelDTO resumen = construirResumen(
                archivoCargado,
                contenido,
                deteccion,
                transformacion,
                registros.size(),
                registros.size(),
                mensaje
        );
        return agregarPendientes(resumen, tratamiento);
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe cargar un archivo hospitalario"
            );
        }

        String nombre = archivo.getOriginalFilename();
        String nombreNormalizado = nombre == null
                ? ""
                : nombre.toLowerCase(Locale.ROOT);
        if (!nombreNormalizado.endsWith(".xlsx")
                && !nombreNormalizado.endsWith(".csv")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo debe tener formato .csv o .xlsx"
            );
        }
    }

    private ArchivoCargado crearArchivoPendiente(
            MultipartFile archivo,
            Usuario usuario,
            Ipress ipress
    ) {
        ArchivoCargado entidad = new ArchivoCargado();
        entidad.setNombreArchivo(archivo.getOriginalFilename());
        entidad.setFormato(extension(archivo.getOriginalFilename()));
        entidad.setFechaCarga(LocalDateTime.now());
        entidad.setEstadoValidacion("PENDIENTE");
        entidad.setEstadoProcesamiento("PENDIENTE");
        entidad.setUsuario(usuario);
        entidad.setIpress(ipress);
        return archivoRepository.save(entidad);
    }

    private String extension(String nombreArchivo) {
        if (nombreArchivo == null) {
            return "DESCONOCIDO";
        }
        int punto = nombreArchivo.lastIndexOf('.');
        return punto < 0
                ? "DESCONOCIDO"
                : nombreArchivo.substring(punto + 1).toUpperCase(Locale.ROOT);
    }

    private ResumenCargaExcelDTO respuestaErrorLectura(
            ArchivoCargado archivo,
            Exception error
    ) {
        archivo.setFormato(FormatoArchivoHospitalario.NO_RECONOCIDO.name());
        archivo.setEstadoValidacion("ERROR");
        archivo.setEstadoProcesamiento("ERROR");
        archivoRepository.save(archivo);

        ErrorValidacionDTO detalle = crearError(
                0,
                "archivo",
                "ERROR_LECTURA",
                "No se pudo leer el archivo: " + mensajeSeguro(error),
                "Verifique la codificacion, el delimitador y que el archivo no este danado."
        );
        ResultadoTransformacionHospitalaria transformacion =
                new ResultadoTransformacionHospitalaria();
        transformacion.getErrores().add(detalle);
        transformacion.incrementarFilasInvalidas();

        return construirResumen(
                archivo,
                new ContenidoArchivoHospitalario(List.of(), List.of()),
                detectorFormato.detectar(List.of()),
                transformacion,
                0,
                0,
                "No se pudo leer el archivo hospitalario."
        );
    }

    private ResumenCargaExcelDTO respuestaFormatoNoReconocido(
            ArchivoCargado archivo,
            ContenidoArchivoHospitalario contenido,
            ResultadoDeteccionFormato deteccion
    ) {
        archivo.setEstadoValidacion("ERROR");
        archivo.setEstadoProcesamiento("ERROR");
        archivoRepository.save(archivo);

        ResultadoTransformacionHospitalaria transformacion =
                new ResultadoTransformacionHospitalaria();
        transformacion.agregarFilasInvalidas(contenido.getFilas().size());
        transformacion.getErrores().add(crearError(
                1,
                "columnas",
                "FORMATO_NO_RECONOCIDO",
                "No se pudo reconocer el formato del archivo.",
                "Use columnas compatibles con FORMATO_INTERNO o DATASET_D1."
        ));

        return construirResumen(
                archivo,
                contenido,
                deteccion,
                transformacion,
                0,
                0,
                "No se pudo reconocer el formato del archivo. Revise las columnas requeridas para FORMATO_INTERNO o DATASET_D1."
        );
    }

    private ResumenCargaExcelDTO respuestaSinRegistros(
            ArchivoCargado archivo,
            ContenidoArchivoHospitalario contenido,
            ResultadoDeteccionFormato deteccion,
            ResultadoTransformacionHospitalaria transformacion,
            String mensaje
    ) {
        archivo.setEstadoValidacion("ERROR");
        archivo.setEstadoProcesamiento("ERROR");
        archivoRepository.save(archivo);

        transformacion.getErrores().add(crearError(
                0,
                "archivo",
                "SIN_REGISTROS_VALIDOS",
                mensaje,
                "Verifique la IPRESS, los periodos y los datos hospitalarios."
        ));
        return construirResumen(
                archivo,
                contenido,
                deteccion,
                transformacion,
                0,
                0,
                mensaje
        );
    }

    private RegistroHospitalario convertirEntidad(
            RegistroHospitalarioImportado origen,
            ArchivoCargado archivo
    ) {
        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setArchivoCargado(archivo);
        registro.setAnio(origen.getAnio());
        registro.setMes(origen.getMes());
        registro.setServicioHospitalario(origen.getServicioHospitalario());
        registro.setIdHospitalizacion(origen.getIdHospitalizacion());
        registro.setSector(origen.getSector());
        registro.setIngresos(origen.getIngresos());
        registro.setEgresos(origen.getEgresos());
        registro.setEstancias(origen.getEstancias());
        registro.setPacientesCama(origen.getPacientesCama());
        registro.setCamasTotales(origen.getCamasTotales());
        registro.setCamasDisponiblesHabilitadas(
                origen.getCamasDisponiblesHabilitadas()
        );
        registro.setTotalCamasDisponibles(origen.getTotalCamasDisponibles());
        registro.setFallecidos(origen.getFallecidos());
        return registro;
    }

    private void completarDatosIpress(
            Ipress ipress,
            RegistroHospitalarioImportado origen
    ) {
        if (!estaVacio(origen.getNombreIpress())) {
            ipress.setNombreIpress(origen.getNombreIpress());
        }
        if (!estaVacio(origen.getCategoriaIpress())) {
            ipress.setCategoriaIpress(origen.getCategoriaIpress());
        }
        if (!estaVacio(origen.getCodigoUbigeo())) {
            ipress.setCodigoUbigeo(origen.getCodigoUbigeo());
        }
        if (!estaVacio(origen.getDepartamento())) {
            ipress.setDepartamento(origen.getDepartamento());
        }
        if (!estaVacio(origen.getProvincia())) {
            ipress.setProvincia(origen.getProvincia());
        }
        if (!estaVacio(origen.getDistrito())) {
            ipress.setDistrito(origen.getDistrito());
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private Usuario obtenerUsuarioAutenticado(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario autenticado no encontrado"
            );
        }
        return usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"
                ));
    }

    private void validarPermisoCarga(
            Usuario usuarioAutenticado,
            Long idUsuario,
            Long idIpress
    ) {
        boolean mismoUsuario = usuarioAutenticado.getIdUsuario() != null
                && usuarioAutenticado.getIdUsuario().equals(idUsuario);
        boolean rolAdmision = usuarioAutenticado.getRol() != null
                && usuarioAutenticado.getRol().getNombreRol() != null
                && ROL_ADMISION.equalsIgnoreCase(
                        usuarioAutenticado.getRol().getNombreRol()
                );
        boolean mismaIpress = usuarioAutenticado.getIpress() != null
                && usuarioAutenticado.getIpress().getIdIpress() != null
                && usuarioAutenticado.getIpress().getIdIpress().equals(idIpress);

        if (!mismoUsuario || !rolAdmision || !mismaIpress) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permiso para cargar informacion de esta IPRESS"
            );
        }
    }

    private ResumenCargaExcelDTO construirResumen(
            ArchivoCargado archivo,
            ContenidoArchivoHospitalario contenido,
            ResultadoDeteccionFormato deteccion,
            ResultadoTransformacionHospitalaria transformacion,
            int totalRegistrosValidos,
            int totalPredicciones,
            String mensaje
    ) {
        ResumenCargaExcelDTO dto = new ResumenCargaExcelDTO();
        dto.setIdArchivo(archivo.getIdArchivo());
        dto.setNombreArchivo(archivo.getNombreArchivo());
        dto.setFormato(archivo.getFormato());
        dto.setFormatoDetectado(deteccion.getFormato().name());
        dto.setEstadoValidacion(archivo.getEstadoValidacion());
        dto.setEstadoProcesamiento(archivo.getEstadoProcesamiento());
        dto.setTotalFilasLeidas(contenido.getFilas().size());
        dto.setTotalFilasInvalidas(transformacion.getTotalFilasInvalidas());
        dto.setTotalRegistrosValidos(totalRegistrosValidos);
        dto.setTotalPrediccionesGeneradas(totalPredicciones);
        dto.setTotalGruposPendientes(0);
        dto.setTotalRegistrosPendientes(0);
        dto.setPendientesCalidad(List.of());
        dto.setRegistrosValidos(totalRegistrosValidos);
        dto.setRegistrosConErrores(transformacion.getTotalFilasInvalidas());
        dto.setErrores(new ArrayList<>(transformacion.getErrores()));
        dto.setAdvertencias(new ArrayList<>(transformacion.getAdvertencias()));
        dto.setColumnasEncontradas(deteccion.getColumnasEncontradas());
        dto.setColumnasMinimasFormatoInterno(
                deteccion.getColumnasMinimasFormatoInterno()
        );
        dto.setColumnasMinimasDatasetD1(
                deteccion.getColumnasMinimasDatasetD1()
        );
        dto.setMensaje(mensaje);
        return dto;
    }

    private void persistirHallazgos(ArchivoCargado archivo, List<HallazgoCalidadImportado> hallazgos) {
        List<HallazgoCalidadDatos> entidades = hallazgos.stream().map(h -> {
            HallazgoCalidadDatos e = new HallazgoCalidadDatos();
            e.setArchivoCargado(archivo); e.setFilaOrigen(h.fila()); e.setCodigoIpress(h.codigoIpress());
            e.setAnio(h.anio()); e.setMes(h.mes()); e.setServicioHospitalario(h.servicioHospitalario());
            e.setRegla(h.regla()); e.setDescripcion(h.descripcion()); e.setEstado("PENDIENTE_VALIDACION");
            e.setVersionPolitica(TratamientoCalidadDatosHospitalarios.VERSION_POLITICA);
            e.setFechaDeteccion(LocalDateTime.now()); return e;
        }).toList();
        if (!entidades.isEmpty()) hallazgoCalidadRepository.saveAll(entidades);
    }

    private ResumenCargaExcelDTO agregarPendientes(ResumenCargaExcelDTO dto, ResultadoTratamientoCalidad tratamiento) {
        dto.setTotalGruposPendientes(tratamiento.gruposPendientes());
        dto.setTotalRegistrosPendientes(tratamiento.registrosPendientes().size());
        Map<String, List<HallazgoCalidadImportado>> hallazgosPorGrupo = tratamiento.hallazgos().stream()
                .collect(Collectors.groupingBy(this::claveHallazgo));
        dto.setPendientesCalidad(tratamiento.registrosPendientes().stream().map(registro -> {
            List<HallazgoCalidadImportado> hallazgos = hallazgosPorGrupo.getOrDefault(
                    claveRegistro(registro),
                    List.of()
            );
            pe.edu.upc.tf_tp1_backend.DTOS.PendienteCalidadDTO p = new pe.edu.upc.tf_tp1_backend.DTOS.PendienteCalidadDTO();
            p.setFila(registro.getNumeroFila()); p.setCodigoIpress(registro.getCodigoIpress());
            p.setAnio(registro.getAnio()); p.setMes(registro.getMes());
            p.setServicioHospitalario(registro.getServicioHospitalario());
            p.setRegla(hallazgos.stream().map(HallazgoCalidadImportado::regla).distinct()
                    .collect(Collectors.joining(", ")));
            p.setMotivo(hallazgos.stream().map(HallazgoCalidadImportado::descripcion).distinct()
                    .collect(Collectors.joining(" ")));
            p.setEstado("PENDIENTE_VALIDACION"); return p;
        }).toList());
        return dto;
    }

    private void persistirRegistrosPendientes(
            ArchivoCargado archivo,
            ResultadoTratamientoCalidad tratamiento
    ) {
        Map<String, List<HallazgoCalidadImportado>> hallazgosPorGrupo = tratamiento.hallazgos().stream()
                .collect(Collectors.groupingBy(this::claveHallazgo));
        List<RegistroPendienteCalidadDatos> entidades = tratamiento.registrosPendientes().stream()
                .map(registro -> {
                    List<HallazgoCalidadImportado> hallazgos = hallazgosPorGrupo.getOrDefault(
                            claveRegistro(registro),
                            List.of()
                    );
                    RegistroPendienteCalidadDatos entidad = new RegistroPendienteCalidadDatos();
                    entidad.setArchivoCargado(archivo);
                    entidad.setFilaOrigen(registro.getNumeroFila());
                    entidad.setCodigoIpress(registro.getCodigoIpress());
                    entidad.setAnio(registro.getAnio());
                    entidad.setMes(registro.getMes());
                    entidad.setServicioHospitalario(registro.getServicioHospitalario());
                    entidad.setReglas(hallazgos.stream().map(HallazgoCalidadImportado::regla)
                            .distinct().collect(Collectors.joining(",")));
                    entidad.setMotivo(hallazgos.stream().map(HallazgoCalidadImportado::descripcion)
                            .distinct().collect(Collectors.joining(" ")));
                    entidad.setVersionPolitica(TratamientoCalidadDatosHospitalarios.VERSION_POLITICA);
                    entidad.setEstado("PENDIENTE_VALIDACION");
                    entidad.setFechaDeteccion(LocalDateTime.now());
                    entidad.setDatosRegistroJson(serializarRegistro(registro));
                    return entidad;
                }).toList();
        if (!entidades.isEmpty()) {
            registroPendienteRepository.saveAll(entidades);
        }
    }

    private String serializarRegistro(RegistroHospitalarioImportado registro) {
        try {
            return objectMapper.writeValueAsString(registro);
        } catch (JacksonException error) {
            throw new IllegalStateException(
                    "No se pudo conservar el registro pendiente de calidad",
                    error
            );
        }
    }

    private String claveHallazgo(HallazgoCalidadImportado hallazgo) {
        return normalizarClave(hallazgo.codigoIpress()) + "|" + hallazgo.anio() + "|"
                + hallazgo.mes() + "|" + normalizarClave(hallazgo.servicioHospitalario());
    }

    private String claveRegistro(RegistroHospitalarioImportado registro) {
        return normalizarClave(registro.getCodigoIpress()) + "|" + registro.getAnio() + "|"
                + registro.getMes() + "|" + normalizarClave(registro.getServicioHospitalario());
    }

    private String normalizarClave(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private ErrorValidacionDTO crearError(
            int fila,
            String campo,
            String tipo,
            String descripcion,
            String recomendacion
    ) {
        ErrorValidacionDTO error = new ErrorValidacionDTO();
        error.setFila(fila);
        error.setCampo(campo);
        error.setTipoError(tipo);
        error.setDescripcion(descripcion);
        error.setRecomendacion(recomendacion);
        return error;
    }

    private String mensajeSeguro(Exception error) {
        String mensaje = error.getMessage();
        return mensaje == null || mensaje.isBlank()
                ? error.getClass().getSimpleName()
                : mensaje;
    }
}
