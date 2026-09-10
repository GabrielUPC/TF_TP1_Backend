package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.NormalizacionPredictiva;
import pe.edu.upc.tf_tp1_backend.DTOS.*;
import pe.edu.upc.tf_tp1_backend.Entities.*;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;
import pe.edu.upc.tf_tp1_backend.Repositories.*;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PrediccionBinariaAutomaticaService {
    static final int MESES_REQUERIDOS = 6;
    static final String MODELO = "modelo_ipress_binario_v1";
    private static final String ROL_HOSPITALIZACION = "ATENCION_HOSPITALIZACION";
    private static final Set<String> ESTADOS_VALIDOS = Set.of("VALIDADO", "VALIDADO_CON_PENDIENTES");

    private final IUsuarioRepository usuarioRepository;
    private final IRegistroHospitalarioRepository registroRepository;
    private final IIndicadorHospitalarioRepository indicadorRepository;
    private final IPrediccionRiesgoRepository prediccionRepository;
    private final CalidadInferenciaRepository calidadRepository;
    private final ModeloPredictivoClientService modeloClient;
    private final IReporteInterfaces reporteService;

    public PrediccionBinariaAutomaticaService(
            IUsuarioRepository usuarioRepository,
            IRegistroHospitalarioRepository registroRepository,
            IIndicadorHospitalarioRepository indicadorRepository,
            IPrediccionRiesgoRepository prediccionRepository,
            CalidadInferenciaRepository calidadRepository,
            ModeloPredictivoClientService modeloClient,
            IReporteInterfaces reporteService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.registroRepository = registroRepository;
        this.indicadorRepository = indicadorRepository;
        this.prediccionRepository = prediccionRepository;
        this.calidadRepository = calidadRepository;
        this.modeloClient = modeloClient;
        this.reporteService = reporteService;
    }

    @Transactional
    public ModeloPrediccionBinariaResponseDTO predecir(
            PrediccionBinariaAutomaticaRequestDTO entrada,
            String correoAutenticado
    ) {
        Usuario usuario = usuarioHospitalario(correoAutenticado);
        Ipress ipress = usuario.getIpress();
        String servicio = textoRequerido(entrada == null ? null : entrada.getServicio(), "servicio");
        YearMonth actual = periodoRequerido(entrada == null ? null : entrada.getPeriodoActual());
        List<RegistroHospitalario> ventana = obtenerVentana(ipress, servicio, actual, true);

        ModeloPrediccionRequestDTO solicitud = new ModeloPrediccionRequestDTO();
        solicitud.setRegistroActual(convertir(ventana.get(5)));
        solicitud.setHistorialUltimosMeses(ventana.subList(0, 5).stream().map(this::convertir).toList());

        ModeloPrediccionBinariaResponseDTO respuesta = modeloClient.predecirBinario(solicitud);
        validarTrazabilidad(respuesta, ipress.getCodigoRenipress(), servicio, actual);
        PrediccionRiesgo prediccion = persistir(ventana.get(5), respuesta, ipress.getIdIpress());
        ReporteDTO reporte = new ReporteDTO();
        reporte.setIdPrediccion(prediccion.getIdPrediccion());
        reporteService.generarReporte(correoAutenticado, reporte);
        return respuesta;
    }

    @Transactional(readOnly = true)
    public CatalogoPrediccionBinariaDTO catalogo(String correoAutenticado) {
        Usuario usuario = usuarioHospitalario(correoAutenticado);
        Ipress ipress = usuario.getIpress();
        List<RegistroHospitalario> todos = registroRepository.buscarPorIpress(ipress.getIdIpress());
        Map<String, List<RegistroHospitalario>> porServicio = todos.stream()
                .filter(r -> !NormalizacionPredictiva.texto(r.getServicioHospitalario()).isBlank())
                .collect(Collectors.groupingBy(r -> NormalizacionPredictiva.texto(r.getServicioHospitalario())));

        List<CatalogoPrediccionBinariaDTO.ServicioPrediccionDTO> servicios = porServicio.entrySet().stream()
                .map(e -> new CatalogoPrediccionBinariaDTO.ServicioPrediccionDTO(
                        e.getKey(), periodosElegibles(ipress, e.getKey(), e.getValue())))
                .filter(e -> !e.getPeriodosDisponibles().isEmpty())
                .sorted(Comparator.comparing(CatalogoPrediccionBinariaDTO.ServicioPrediccionDTO::getServicio))
                .toList();

        CatalogoPrediccionBinariaDTO dto = new CatalogoPrediccionBinariaDTO();
        dto.setCodigoIpress(NormalizacionPredictiva.renipress(ipress.getCodigoRenipress()));
        dto.setNombreIpress(ipress.getNombreIpress());
        dto.setServicios(servicios);
        return dto;
    }

    private List<String> periodosElegibles(Ipress ipress, String servicio, List<RegistroHospitalario> registros) {
        Set<YearMonth> candidatos = registros.stream().map(this::periodo)
                .collect(Collectors.toCollection(TreeSet::new));
        List<String> resultado = new ArrayList<>();
        for (YearMonth actual : candidatos) {
            try {
                resolverVentana(ipress, servicio, actual, registros, false);
                resultado.add(actual.toString());
            } catch (PrediccionBinariaAutomaticaException ignored) {
                // Un periodo no elegible se omite del catalogo; no se inventan meses ni datos.
            }
        }
        return resultado;
    }

    private List<RegistroHospitalario> obtenerVentana(
            Ipress ipress, String servicio, YearMonth actual, boolean distinguirServicio
    ) {
        List<RegistroHospitalario> todos = registroRepository.buscarPorIpress(ipress.getIdIpress());
        return resolverVentana(ipress, servicio, actual, todos, distinguirServicio);
    }

    private List<RegistroHospitalario> resolverVentana(
            Ipress ipress,
            String servicioSolicitado,
            YearMonth actual,
            List<RegistroHospitalario> fuente,
            boolean distinguirServicio
    ) {
        String servicio = NormalizacionPredictiva.texto(servicioSolicitado);
        YearMonth desde = actual.minusMonths(5);
        List<RegistroHospitalario> delServicio = fuente.stream()
                .filter(r -> servicio.equals(NormalizacionPredictiva.texto(r.getServicioHospitalario())))
                .toList();
        if (delServicio.isEmpty() && distinguirServicio) {
            throw error(HttpStatus.NOT_FOUND, "SERVICIO_NO_EXISTE",
                    "La IPRESS autenticada no tiene registros para el servicio indicado.");
        }
        if (calidadRepository.existePendiente(ipress.getIdIpress(), servicio, desde, actual)) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "CALIDAD_PENDIENTE",
                    "Uno o mas meses requeridos estan pendientes de validacion de calidad.");
        }

        Map<YearMonth, List<RegistroHospitalario>> porPeriodo = delServicio.stream()
                .filter(r -> !periodo(r).isBefore(desde) && !periodo(r).isAfter(actual))
                .collect(Collectors.groupingBy(this::periodo));
        List<YearMonth> requeridos = periodosRequeridos(actual);
        Map<YearMonth, RegistroHospitalario> resueltos = new HashMap<>();
        for (YearMonth periodo : requeridos) {
            List<RegistroHospitalario> versiones = porPeriodo.getOrDefault(periodo, List.of());
            if (!versiones.isEmpty()) resueltos.put(periodo, resolverVersiones(periodo, versiones));
        }
        List<YearMonth> faltantes = requeridos.stream().filter(p -> !resueltos.containsKey(p)).toList();
        if (!faltantes.isEmpty()) {
            boolean hueco = faltantes.stream().anyMatch(f -> requeridos.stream().anyMatch(
                    p -> p.isBefore(f) && resueltos.containsKey(p))
                    && requeridos.stream().anyMatch(p -> p.isAfter(f) && resueltos.containsKey(p)));
            throw new PrediccionBinariaAutomaticaException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    hueco ? "MES_INTERMEDIO_FALTANTE" : "HISTORIAL_INSUFICIENTE",
                    hueco ? "Falta un mes intermedio; no se permite saltar periodos."
                            : "No existen los seis meses consecutivos requeridos.",
                    faltantes.stream().map(YearMonth::toString).toList());
        }
        List<RegistroHospitalario> ventana = requeridos.stream().map(resueltos::get).toList();
        if (ventana.stream().anyMatch(r -> !NormalizacionPredictiva.servicioEnAlcance(r.getIdHospitalizacion()))) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "SERVICIO_FUERA_ALCANCE",
                    "La ventana no pertenece completamente a las familias 24xx o 25xx.");
        }
        return ventana;
    }

    private RegistroHospitalario resolverVersiones(
            YearMonth periodo, List<RegistroHospitalario> versiones
    ) {
        List<RegistroHospitalario> validadas = versiones.stream()
                .filter(this::archivoValidado)
                .toList();
        if (validadas.isEmpty()) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "CALIDAD_INVALIDA",
                    "El periodo " + periodo + " no procede de un archivo validado.");
        }
        String referencia = firmaDatos(validadas.get(0));
        if (validadas.stream().anyMatch(r -> !referencia.equals(firmaDatos(r)))) {
            throw new PrediccionBinariaAutomaticaException(
                    HttpStatus.CONFLICT, "PERIODO_CONFLICTIVO",
                    "Hay versiones validadas diferentes para " + periodo + "; la inferencia queda bloqueada.",
                    List.of(periodo.toString()));
        }
        return validadas.stream().max(Comparator
                .comparing((RegistroHospitalario r) -> Optional.ofNullable(r.getArchivoCargado().getFechaCarga())
                        .orElse(LocalDateTime.MIN))
                .thenComparing(r -> Optional.ofNullable(r.getArchivoCargado().getIdArchivo()).orElse(0L))
                .thenComparing(r -> Optional.ofNullable(r.getIdRegistro()).orElse(0)))
                .orElseThrow();
    }

    private String firmaDatos(RegistroHospitalario r) {
        return List.of(
                NormalizacionPredictiva.texto(r.getServicioHospitalario()),
                NormalizacionPredictiva.texto(r.getIdHospitalizacion()),
                Objects.toString(r.getIngresos(), ""), Objects.toString(r.getEgresos(), ""),
                Objects.toString(r.getEstancias(), ""), Objects.toString(r.getPacientesCama(), ""),
                Objects.toString(r.getCamasTotales(), ""),
                Objects.toString(r.getCamasDisponiblesHabilitadas(), ""),
                Objects.toString(r.getTotalCamasDisponibles(), ""),
                Objects.toString(r.getFallecidos(), ""), NormalizacionPredictiva.texto(r.getSector())
        ).toString();
    }

    private boolean archivoValidado(RegistroHospitalario r) {
        String estado = r.getArchivoCargado() == null ? ""
                : NormalizacionPredictiva.texto(r.getArchivoCargado().getEstadoValidacion());
        return ESTADOS_VALIDOS.contains(estado);
    }

    private PrediccionRiesgo persistir(
            RegistroHospitalario actual,
            ModeloPrediccionBinariaResponseDTO respuesta,
            Long idIpress
    ) {
        IndicadorHospitalario indicador = indicadorRepository
                .findByRegistroHospitalario_IdRegistro(actual.getIdRegistro())
                .orElseThrow(() -> error(HttpStatus.UNPROCESSABLE_CONTENT, "INDICADOR_NO_DISPONIBLE",
                        "El periodo actual no tiene indicadores calculados."));
        PrediccionRiesgo entidad = prediccionRepository
                .findByIndicadorHospitalario_IdIndicador(indicador.getIdIndicador())
                .orElseGet(PrediccionRiesgo::new);
        entidad.setIndicadorHospitalario(indicador);
        entidad.setProbabilidadAltaPresion(respuesta.getProbabilidadAltaPresion());
        entidad.setThreshold(respuesta.getThreshold());
        entidad.setAlertaAltaPresion(respuesta.getAlertaAltaPresion());
        entidad.setNivelRiesgo(respuesta.getNivelRiesgo().name());
        entidad.setDescripcionRiesgo(respuesta.getDescripcionRiesgo());
        entidad.setPeriodoPredicho(respuesta.getPeriodoPredicho());
        entidad.setModeloUtilizado(respuesta.getModelo());
        entidad.setFechaPrediccion(LocalDateTime.now());
        entidad.setVigente(true);
        entidad.setMotivoNoVigente(null);

        String servicio = NormalizacionPredictiva.texto(actual.getServicioHospitalario());
        List<PrediccionRiesgo> anteriores = prediccionRepository
                .findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_Ipress_IdIpress(idIpress);
        List<PrediccionRiesgo> duplicadas = anteriores.stream()
                .filter(p -> !Objects.equals(p.getIdPrediccion(), entidad.getIdPrediccion()))
                .filter(p -> MODELO.equals(p.getModeloUtilizado()))
                .filter(p -> respuesta.getPeriodoPredicho().equals(p.getPeriodoPredicho()))
                .filter(p -> servicio.equals(NormalizacionPredictiva.texto(
                        p.getIndicadorHospitalario().getRegistroHospitalario().getServicioHospitalario())))
                .toList();
        duplicadas.forEach(p -> {
            p.setVigente(false);
            p.setMotivoNoVigente("Reemplazada por una prediccion binaria mas reciente para la misma clave.");
        });
        if (!duplicadas.isEmpty()) prediccionRepository.saveAll(duplicadas);
        return prediccionRepository.save(entidad);
    }

    private ModeloDatosHospitalariosDTO convertir(RegistroHospitalario r) {
        Ipress ipress = r.getArchivoCargado().getIpress();
        YearMonth p = periodo(r);
        ModeloDatosHospitalariosDTO d = new ModeloDatosHospitalariosDTO();
        d.setAnio(p.getYear()); d.setMes(p.getMonthValue());
        d.setUbigeo(textoBase(ipress.getCodigoUbigeo(), "codigo_ubigeo"));
        d.setDepartamento(textoBase(ipress.getDepartamento(), "departamento"));
        d.setProvincia(textoBase(ipress.getProvincia(), "provincia"));
        d.setDistrito(textoBase(ipress.getDistrito(), "distrito"));
        d.setSector(r.getSector() == null || r.getSector().isBlank()
                ? "MINSA" : NormalizacionPredictiva.texto(r.getSector()));
        d.setCategoriaIpress(textoBase(ipress.getCategoriaIpress(), "categoria_ipress"));
        d.setCodigoIpress(NormalizacionPredictiva.renipress(ipress.getCodigoRenipress()));
        d.setIdHospitalizacion(textoBase(r.getIdHospitalizacion(), "id_hospitalizacion"));
        d.setServicioHospitalizacion(textoBase(r.getServicioHospitalario(), "servicio_hospitalizacion"));
        d.setTotalIngresos(numeroBase(r.getIngresos(), "ingresos"));
        d.setTotalEgresos(numeroBase(r.getEgresos(), "egresos"));
        d.setTotalEstancias(numeroBase(r.getEstancias(), "estancias"));
        d.setTotalPacientesCamas(numeroBase(r.getPacientesCama(), "pacientes_cama"));
        d.setTotalCamas(numeroBase(r.getCamasTotales(), "camas_totales"));
        d.setTotalCamasDisponibles(r.getTotalCamasDisponibles() != null
                ? numeroBase(r.getTotalCamasDisponibles(), "total_camas_disponibles")
                : numeroBase(r.getCamasDisponiblesHabilitadas(), "camas_disponibles_habilitadas")
                    * p.lengthOfMonth());
        d.setTotalFallecidos(r.getFallecidos() == null ? 0.0 : numeroBase(r.getFallecidos(), "fallecidos"));
        return d;
    }

    private void validarTrazabilidad(
            ModeloPrediccionBinariaResponseDTO r, String codigo, String servicio, YearMonth actual
    ) {
        if (r == null
                || !NormalizacionPredictiva.renipress(codigo).equals(r.getCodigoIpress())
                || !NormalizacionPredictiva.texto(servicio).equals(
                        NormalizacionPredictiva.texto(r.getServicioHospitalizacion()))
                || !actual.toString().equals(r.getPeriodoActual())
                || !actual.plusMonths(1).toString().equals(r.getPeriodoPredicho())
                || !MODELO.equals(r.getModelo())) {
            throw error(HttpStatus.BAD_GATEWAY, "RESPUESTA_MODELO_INCONSISTENTE",
                    "La trazabilidad devuelta por FastAPI no coincide con la solicitud.");
        }
    }

    private Usuario usuarioHospitalario(String correo) {
        if (correo == null || correo.isBlank()) {
            throw error(HttpStatus.UNAUTHORIZED, "USUARIO_NO_AUTORIZADO", "No existe el usuario autenticado.");
        }
        Usuario u = usuarioRepository.findByCorreo(correo.trim())
                .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "USUARIO_NO_AUTORIZADO",
                        "No existe el usuario autenticado."));
        String rol = u.getRol() == null ? "" : NormalizacionPredictiva.texto(u.getRol().getNombreRol());
        if (!Boolean.TRUE.equals(u.getEstado()) || !ROL_HOSPITALIZACION.equals(rol) || u.getIpress() == null) {
            throw error(HttpStatus.FORBIDDEN, "IPRESS_NO_AUTORIZADA",
                    "El usuario no tiene una IPRESS hospitalaria habilitada.");
        }
        return u;
    }

    private List<YearMonth> periodosRequeridos(YearMonth actual) {
        List<YearMonth> p = new ArrayList<>();
        for (int i = 5; i >= 0; i--) p.add(actual.minusMonths(i));
        return p;
    }
    private YearMonth periodo(RegistroHospitalario r) {
        try { return YearMonth.of(r.getAnio(), r.getMes()); }
        catch (RuntimeException e) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "DATOS_INCOMPLETOS", "Un registro tiene periodo invalido.");
        }
    }
    private YearMonth periodoRequerido(String v) {
        try {
            if (v == null || !v.trim().matches("\\d{4}-(0[1-9]|1[0-2])")) throw new DateTimeParseException("", "", 0);
            return YearMonth.parse(v.trim());
        } catch (DateTimeParseException e) {
            throw error(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", "periodo_actual debe tener formato YYYY-MM.");
        }
    }
    private String textoRequerido(String v, String campo) {
        String n = NormalizacionPredictiva.texto(v);
        if (n.isBlank()) throw error(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "El campo " + campo + " es obligatorio.");
        return n;
    }
    private String textoBase(String v, String campo) {
        String n = NormalizacionPredictiva.texto(v);
        if (n.isBlank()) throw error(HttpStatus.UNPROCESSABLE_CONTENT, "DATOS_INCOMPLETOS",
                "El campo " + campo + " esta incompleto.");
        return n;
    }
    private Double numeroBase(Integer v, String campo) {
        if (v == null || v < 0) throw error(HttpStatus.UNPROCESSABLE_CONTENT, "DATOS_INCOMPLETOS",
                "El campo " + campo + " esta ausente o es invalido.");
        return v.doubleValue();
    }
    private PrediccionBinariaAutomaticaException error(HttpStatus s, String c, String m) {
        return new PrediccionBinariaAutomaticaException(s, c, m);
    }
}
