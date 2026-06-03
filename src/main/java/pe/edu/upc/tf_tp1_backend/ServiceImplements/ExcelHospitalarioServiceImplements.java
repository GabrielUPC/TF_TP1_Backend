package pe.edu.upc.tf_tp1_backend.ServiceImplements;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ErrorValidacionDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ResumenCargaExcelDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IIpressRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IExcelHospitalarioInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIndicadorHospitalarioInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExcelHospitalarioServiceImplements implements IExcelHospitalarioInterfaces {

    @Autowired
    private IArchivoCargadoRepository aR;

    @Autowired
    private IReporteInterfaces reporteService;

    @Autowired
    private IRegistroHospitalarioRepository rR;

    @Autowired
    private IUsuarioRepository uR;

    @Autowired
    private IIpressRepository iR;

    @Autowired
    private IIndicadorHospitalarioInterfaces indicadorService;

    @Autowired
    private IPrediccionRiesgoInterfaces prediccionService;


    private static final List<String> COLUMNAS_REQUERIDAS = Arrays.asList(
            "codigo_renipress",
            "anio",
            "mes",
            "servicio_hospitalario",
            "ingresos",
            "egresos",
            "estancias",
            "pacientes_cama",
            "camas_totales",
            "camas_disponibles_habilitadas"
    );

    @Override
    public byte[] generarPlantillaExcel() {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet hoja = workbook.createSheet("hospitalizacion");

            Row cabecera = hoja.createRow(0);

            CellStyle estiloCabecera = workbook.createCellStyle();
            Font fuente = workbook.createFont();
            fuente.setBold(true);
            estiloCabecera.setFont(fuente);

            for (int i = 0; i < COLUMNAS_REQUERIDAS.size(); i++) {
                Cell cell = cabecera.createCell(i);
                cell.setCellValue(COLUMNAS_REQUERIDAS.get(i));
                cell.setCellStyle(estiloCabecera);
                hoja.autoSizeColumn(i);
            }

            Row ejemplo = hoja.createRow(1);
            ejemplo.createCell(0).setCellValue("00001234");
            ejemplo.createCell(1).setCellValue(2026);
            ejemplo.createCell(2).setCellValue(1);
            ejemplo.createCell(3).setCellValue("Hospitalización Medicina");
            ejemplo.createCell(4).setCellValue(120);
            ejemplo.createCell(5).setCellValue(110);
            ejemplo.createCell(6).setCellValue(550);
            ejemplo.createCell(7).setCellValue(95);
            ejemplo.createCell(8).setCellValue(100);
            ejemplo.createCell(9).setCellValue(85);

            for (int i = 0; i < COLUMNAS_REQUERIDAS.size(); i++) {
                hoja.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al generar la plantilla Excel"
            );
        }
    }

    @Override
    @Transactional
    public ResumenCargaExcelDTO cargarValidarYProcesarExcel(MultipartFile archivo, Long idUsuario, Long idIpress) {

        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe cargar un archivo Excel");
        }

        String nombreArchivo = archivo.getOriginalFilename();

        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".xlsx")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe tener formato .xlsx");
        }

        Usuario usuario = uR.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        Ipress ipress = iR.findById(idIpress)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "IPRESS no encontrada"
                ));

        ArchivoCargado archivoCargado = new ArchivoCargado();
        archivoCargado.setNombreArchivo(nombreArchivo);
        archivoCargado.setFormato("xlsx");
        archivoCargado.setFechaCarga(LocalDateTime.now());
        archivoCargado.setEstadoValidacion("PENDIENTE");
        archivoCargado.setEstadoProcesamiento("PENDIENTE");
        archivoCargado.setUsuario(usuario);
        archivoCargado.setIpress(ipress);

        archivoCargado = aR.save(archivoCargado);

        List<ErrorValidacionDTO> errores = new ArrayList<>();
        List<RegistroHospitalario> registrosValidos = new ArrayList<>();
        Set<String> clavesDuplicadas = new HashSet<>();

        int totalFilasLeidas = 0;

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {

            Sheet hoja = workbook.getSheetAt(0);

            if (hoja == null) {
                agregarError(errores, 0, "archivo", "HOJA_NO_ENCONTRADA",
                        "El archivo no contiene una hoja válida.",
                        "Verifique que el Excel tenga una hoja con datos.");
            } else {

                Row filaCabecera = hoja.getRow(0);

                Map<String, Integer> columnas = obtenerColumnas(filaCabecera);

                validarColumnasObligatorias(columnas, errores);

                if (errores.isEmpty()) {

                    for (int i = 1; i <= hoja.getLastRowNum(); i++) {

                        Row fila = hoja.getRow(i);

                        if (fila == null || filaVacia(fila)) {
                            continue;
                        }

                        totalFilasLeidas++;

                        int numeroFilaExcel = i + 1;

                        String codigoRenipress = obtenerTexto(fila, columnas.get("codigo_renipress"));
                        Integer anio = obtenerEntero(fila, columnas.get("anio"));
                        Integer mes = obtenerEntero(fila, columnas.get("mes"));
                        String servicioHospitalario = obtenerTexto(fila, columnas.get("servicio_hospitalario"));
                        Integer ingresos = obtenerEntero(fila, columnas.get("ingresos"));
                        Integer egresos = obtenerEntero(fila, columnas.get("egresos"));
                        Integer estancias = obtenerEntero(fila, columnas.get("estancias"));
                        Integer pacientesCama = obtenerEntero(fila, columnas.get("pacientes_cama"));
                        Integer camasTotales = obtenerEntero(fila, columnas.get("camas_totales"));
                        Integer camasDisponibles = obtenerEntero(fila, columnas.get("camas_disponibles_habilitadas"));

                        validarFila(
                                errores,
                                numeroFilaExcel,
                                codigoRenipress,
                                anio,
                                mes,
                                servicioHospitalario,
                                ingresos,
                                egresos,
                                estancias,
                                pacientesCama,
                                camasTotales,
                                camasDisponibles,
                                ipress,
                                clavesDuplicadas
                        );

                        boolean filaConError = errores.stream()
                                .anyMatch(e -> e.getFila().equals(numeroFilaExcel));

                        if (!filaConError) {
                            RegistroHospitalario registro = new RegistroHospitalario();
                            registro.setArchivoCargado(archivoCargado);
                            registro.setAnio(anio);
                            registro.setMes(mes);
                            registro.setServicioHospitalario(servicioHospitalario);
                            registro.setIngresos(ingresos);
                            registro.setEgresos(egresos);
                            registro.setEstancias(estancias);
                            registro.setPacientesCama(pacientesCama);
                            registro.setCamasTotales(camasTotales);
                            registro.setCamasDisponiblesHabilitadas(camasDisponibles);

                            registrosValidos.add(registro);
                        }
                    }
                }
            }

        } catch (Exception e) {
            agregarError(errores, 0, "archivo", "ERROR_LECTURA",
                    "No se pudo leer correctamente el archivo Excel.",
                    "Verifique que el archivo no esté dañado y que tenga formato .xlsx.");
        }

        if (!errores.isEmpty()) {
            archivoCargado.setEstadoValidacion("ERROR");
            archivoCargado.setEstadoProcesamiento("ERROR");
            aR.save(archivoCargado);

            return construirResumen(
                    archivoCargado,
                    totalFilasLeidas,
                    registrosValidos.size(),
                    errores,
                    "El archivo contiene errores. Corrija la información y vuelva a cargarlo."
            );
        }

        if (registrosValidos.isEmpty()) {
            agregarError(errores, 0, "archivo", "SIN_REGISTROS",
                    "El archivo no contiene registros válidos.",
                    "Agregue al menos una fila de datos hospitalarios.");

            archivoCargado.setEstadoValidacion("ERROR");
            archivoCargado.setEstadoProcesamiento("ERROR");
            aR.save(archivoCargado);

            return construirResumen(
                    archivoCargado,
                    totalFilasLeidas,
                    0,
                    errores,
                    "El archivo no contiene registros válidos."
            );
        }

        rR.saveAll(registrosValidos);

        archivoCargado.setEstadoValidacion("VALIDADO");
        archivoCargado.setEstadoProcesamiento("PROCESADO");
        aR.save(archivoCargado);

        indicadorService.calcularPorArchivo(archivoCargado.getIdArchivo());
        prediccionService.predecirPorArchivo(archivoCargado.getIdArchivo());
        reporteService.generarPorArchivo(archivoCargado.getIdArchivo(), usuario.getIdUsuario());

        return construirResumen(
                archivoCargado,
                totalFilasLeidas,
                registrosValidos.size(),
                errores,
                "Archivo cargado, validado y procesado correctamente."
        );
    }

    private Map<String, Integer> obtenerColumnas(Row filaCabecera) {

        Map<String, Integer> columnas = new HashMap<>();

        if (filaCabecera == null) {
            return columnas;
        }

        for (Cell cell : filaCabecera) {
            String nombreColumna = obtenerTextoCelda(cell);

            if (nombreColumna != null) {
                columnas.put(normalizar(nombreColumna), cell.getColumnIndex());
            }
        }

        return columnas;
    }

    private void validarColumnasObligatorias(Map<String, Integer> columnas, List<ErrorValidacionDTO> errores) {

        for (String columna : COLUMNAS_REQUERIDAS) {
            if (!columnas.containsKey(columna)) {
                agregarError(errores, 1, columna, "COLUMNA_OBLIGATORIA",
                        "Falta la columna obligatoria: " + columna,
                        "Agregue la columna " + columna + " en la primera fila del Excel.");
            }
        }
    }

    private void validarFila(
            List<ErrorValidacionDTO> errores,
            Integer fila,
            String codigoRenipress,
            Integer anio,
            Integer mes,
            String servicioHospitalario,
            Integer ingresos,
            Integer egresos,
            Integer estancias,
            Integer pacientesCama,
            Integer camasTotales,
            Integer camasDisponibles,
            Ipress ipress,
            Set<String> clavesDuplicadas
    ) {

        if (codigoRenipress == null || codigoRenipress.isBlank()) {
            agregarError(errores, fila, "codigo_renipress", "CAMPO_VACIO",
                    "El código RENIPRESS está vacío.",
                    "Ingrese el código RENIPRESS correspondiente a la IPRESS.");
        } else if (ipress.getCodigoRenipress() != null
                && !ipress.getCodigoRenipress().equalsIgnoreCase(codigoRenipress)) {
            agregarError(errores, fila, "codigo_renipress", "IPRESS_NO_COINCIDE",
                    "El código RENIPRESS del archivo no coincide con la IPRESS seleccionada.",
                    "Verifique que el archivo corresponda a la IPRESS indicada.");
        }

        if (anio == null) {
            agregarError(errores, fila, "anio", "CAMPO_VACIO",
                    "El año está vacío o no es numérico.",
                    "Ingrese un año válido, por ejemplo 2026.");
        } else if (anio < 2000 || anio > 2100) {
            agregarError(errores, fila, "anio", "VALOR_INVALIDO",
                    "El año se encuentra fuera del rango permitido.",
                    "Ingrese un año válido entre 2000 y 2100.");
        }

        if (mes == null) {
            agregarError(errores, fila, "mes", "CAMPO_VACIO",
                    "El mes está vacío o no es numérico.",
                    "Ingrese un mes entre 1 y 12.");
        } else if (mes < 1 || mes > 12) {
            agregarError(errores, fila, "mes", "VALOR_INVALIDO",
                    "El mes debe estar entre 1 y 12.",
                    "Corrija el valor del mes.");
        }

        if (servicioHospitalario == null || servicioHospitalario.isBlank()) {
            agregarError(errores, fila, "servicio_hospitalario", "CAMPO_VACIO",
                    "El servicio hospitalario está vacío.",
                    "Ingrese el servicio hospitalario correspondiente.");
        }

        validarEnteroNoNegativo(errores, fila, "ingresos", ingresos);
        validarEnteroNoNegativo(errores, fila, "egresos", egresos);
        validarEnteroNoNegativo(errores, fila, "estancias", estancias);
        validarEnteroNoNegativo(errores, fila, "pacientes_cama", pacientesCama);
        validarEnteroNoNegativo(errores, fila, "camas_totales", camasTotales);
        validarEnteroNoNegativo(errores, fila, "camas_disponibles_habilitadas", camasDisponibles);

        if (camasTotales != null && camasDisponibles != null && camasDisponibles > camasTotales) {
            agregarError(errores, fila, "camas_disponibles_habilitadas", "INCONSISTENCIA",
                    "Las camas disponibles o habilitadas no pueden ser mayores que las camas totales.",
                    "Revise los valores de camas totales y camas disponibles/habilitadas.");
        }

        if (codigoRenipress != null && anio != null && mes != null && servicioHospitalario != null) {
            String clave = codigoRenipress.trim().toLowerCase() + "|" + anio + "|" + mes + "|" + servicioHospitalario.trim().toLowerCase();

            if (clavesDuplicadas.contains(clave)) {
                agregarError(errores, fila, "registro", "DUPLICADO",
                        "Existe un registro duplicado para la misma IPRESS, año, mes y servicio hospitalario.",
                        "Elimine o consolide los registros duplicados antes de procesar.");
            } else {
                clavesDuplicadas.add(clave);
            }
        }
    }

    private void validarEnteroNoNegativo(List<ErrorValidacionDTO> errores, Integer fila, String campo, Integer valor) {

        if (valor == null) {
            agregarError(errores, fila, campo, "CAMPO_VACIO",
                    "El campo " + campo + " está vacío o no es numérico.",
                    "Ingrese un valor numérico entero mayor o igual a cero.");
        } else if (valor < 0) {
            agregarError(errores, fila, campo, "VALOR_NEGATIVO",
                    "El campo " + campo + " no puede tener valores negativos.",
                    "Corrija el valor ingresado.");
        }
    }

    private void agregarError(
            List<ErrorValidacionDTO> errores,
            Integer fila,
            String campo,
            String tipoError,
            String descripcion,
            String recomendacion
    ) {
        ErrorValidacionDTO error = new ErrorValidacionDTO();
        error.setFila(fila);
        error.setCampo(campo);
        error.setTipoError(tipoError);
        error.setDescripcion(descripcion);
        error.setRecomendacion(recomendacion);

        errores.add(error);
    }

    private ResumenCargaExcelDTO construirResumen(
            ArchivoCargado archivoCargado,
            Integer totalFilasLeidas,
            Integer registrosValidos,
            List<ErrorValidacionDTO> errores,
            String mensaje
    ) {

        ResumenCargaExcelDTO dto = new ResumenCargaExcelDTO();

        dto.setIdArchivo(archivoCargado.getIdArchivo());
        dto.setNombreArchivo(archivoCargado.getNombreArchivo());
        dto.setFormato(archivoCargado.getFormato());
        dto.setEstadoValidacion(archivoCargado.getEstadoValidacion());
        dto.setEstadoProcesamiento(archivoCargado.getEstadoProcesamiento());
        dto.setTotalFilasLeidas(totalFilasLeidas);
        dto.setRegistrosValidos(registrosValidos);
        dto.setRegistrosConErrores(errores.size());
        dto.setMensaje(mensaje);
        dto.setErrores(errores);

        return dto;
    }

    private String obtenerTexto(Row fila, Integer indice) {

        if (indice == null) {
            return null;
        }

        Cell cell = fila.getCell(indice);

        return obtenerTextoCelda(cell);
    }

    private String obtenerTextoCelda(Cell cell) {

        if (cell == null) {
            return null;
        }

        DataFormatter formatter = new DataFormatter();

        String valor = formatter.formatCellValue(cell);

        if (valor == null) {
            return null;
        }

        return valor.trim();
    }

    private Integer obtenerEntero(Row fila, Integer indice) {

        if (indice == null) {
            return null;
        }

        Cell cell = fila.getCell(indice);

        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }

            String valor = obtenerTextoCelda(cell);

            if (valor == null || valor.isBlank()) {
                return null;
            }

            valor = valor.replace(",", "").trim();

            return Integer.parseInt(valor);

        } catch (Exception e) {
            return null;
        }
    }

    private boolean filaVacia(Row fila) {

        for (int i = 0; i < COLUMNAS_REQUERIDAS.size(); i++) {
            Cell cell = fila.getCell(i);
            String valor = obtenerTextoCelda(cell);

            if (valor != null && !valor.isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto.trim()
                .toLowerCase()
                .replace(" ", "_")
                .replace("-", "_")
                .replace(".", "")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");
    }
}