package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloDatosHospitalariosDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionBinariaResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionRequestDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionBinariaAutomaticaRequestDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;
import pe.edu.upc.tf_tp1_backend.Repositories.CalidadInferenciaRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IIpressRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class PrediccionBinariaAutomaticaService {
    private static final int MESES_REQUERIDOS = 6;
    private static final Set<String> ESTADOS_ARCHIVO_VALIDOS =
            Set.of("VALIDADO", "VALIDADO_CON_PENDIENTES");

    private final IIpressRepository ipressRepository;
    private final IRegistroHospitalarioRepository registroRepository;
    private final CalidadInferenciaRepository calidadRepository;
    private final ModeloPredictivoClientService modeloClient;

    public PrediccionBinariaAutomaticaService(
            IIpressRepository ipressRepository,
            IRegistroHospitalarioRepository registroRepository,
            CalidadInferenciaRepository calidadRepository,
            ModeloPredictivoClientService modeloClient
    ) {
        this.ipressRepository = ipressRepository;
        this.registroRepository = registroRepository;
        this.calidadRepository = calidadRepository;
        this.modeloClient = modeloClient;
    }

    @Transactional(readOnly = true)
    public ModeloPrediccionBinariaResponseDTO predecir(PrediccionBinariaAutomaticaRequestDTO entrada) {
        String codigoIpress = textoRequerido(entrada == null ? null : entrada.getCodigoIpress(), "co_ipress");
        String servicio = textoRequerido(entrada == null ? null : entrada.getServicio(), "servicio");
        YearMonth periodoActual = periodoRequerido(entrada == null ? null : entrada.getPeriodoActual());
        YearMonth periodoDesde = periodoActual.minusMonths(MESES_REQUERIDOS - 1L);

        Ipress ipress = ipressRepository.findByCodigoRenipressIgnoreCase(codigoIpress)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "IPRESS_NO_EXISTE",
                        "No existe una IPRESS con el codigo indicado."));

        List<RegistroHospitalario> encontrados = registroRepository.buscarVentanaInferencia(
                ipress.getIdIpress(), servicio,
                periodoDesde.getYear(), periodoDesde.getMonthValue(),
                periodoActual.getYear(), periodoActual.getMonthValue()
        );

        if (calidadRepository.existePendiente(ipress.getIdIpress(), servicio, periodoDesde, periodoActual)) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "CALIDAD_PENDIENTE",
                    "Uno o mas meses requeridos estan pendientes de validacion de calidad.");
        }

        if (encontrados.isEmpty()
                && !registroRepository.existsByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(
                ipress.getIdIpress(), servicio)) {
            throw error(HttpStatus.NOT_FOUND, "SERVICIO_NO_EXISTE",
                    "La IPRESS no tiene registros para el servicio indicado.");
        }

        validarDuplicados(encontrados);
        validarAlcance(encontrados);
        validarEstadosArchivo(encontrados);

        Map<YearMonth, RegistroHospitalario> porPeriodo = new HashMap<>();
        for (RegistroHospitalario registro : encontrados) {
            porPeriodo.put(periodo(registro), registro);
        }

        List<YearMonth> requeridos = periodosRequeridos(periodoActual);
        List<YearMonth> faltantes = requeridos.stream().filter(p -> !porPeriodo.containsKey(p)).toList();
        if (!faltantes.isEmpty()) {
            String codigo = existeHuecoIntermedio(requeridos, porPeriodo, faltantes)
                    ? "MES_INTERMEDIO_FALTANTE" : "HISTORIAL_INSUFICIENTE";
            String mensaje = codigo.equals("MES_INTERMEDIO_FALTANTE")
                    ? "Falta uno o mas meses intermedios; no se permite saltar periodos."
                    : "No existen los seis meses consecutivos requeridos para inferencia.";
            throw new PrediccionBinariaAutomaticaException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    codigo,
                    mensaje,
                    faltantes.stream().map(YearMonth::toString).toList()
            );
        }

        List<RegistroHospitalario> ordenados = requeridos.stream().map(porPeriodo::get).toList();
        ModeloPrediccionRequestDTO solicitudModelo = new ModeloPrediccionRequestDTO();
        solicitudModelo.setRegistroActual(convertir(ordenados.get(MESES_REQUERIDOS - 1)));
        solicitudModelo.setHistorialUltimosMeses(
                ordenados.subList(0, MESES_REQUERIDOS - 1).stream().map(this::convertir).toList()
        );

        ModeloPrediccionBinariaResponseDTO respuesta = modeloClient.predecirBinario(solicitudModelo);
        validarTrazabilidad(respuesta, codigoIpress, servicio, periodoActual);
        return respuesta;
    }

    private void validarDuplicados(List<RegistroHospitalario> registros) {
        Map<YearMonth, Integer> cantidades = new HashMap<>();
        for (RegistroHospitalario registro : registros) {
            cantidades.merge(periodo(registro), 1, Integer::sum);
        }
        List<String> duplicados = cantidades.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(e -> e.getKey().toString())
                .sorted()
                .toList();
        if (!duplicados.isEmpty()) {
            throw new PrediccionBinariaAutomaticaException(
                    HttpStatus.CONFLICT,
                    "PERIODO_DUPLICADO",
                    "Existe mas de un registro para IPRESS, servicio y periodo; no se elegira uno arbitrariamente.",
                    duplicados
            );
        }
    }

    private void validarAlcance(List<RegistroHospitalario> registros) {
        boolean fuera = registros.stream().anyMatch(registro -> {
            String id = registro.getIdHospitalizacion();
            if (id == null) return true;
            String normalizado = id.trim();
            return !(normalizado.startsWith("24") || normalizado.startsWith("25"));
        });
        if (fuera) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "SERVICIO_FUERA_ALCANCE",
                    "El servicio no pertenece a las familias hospitalarias 24xx o 25xx.");
        }
    }

    private void validarEstadosArchivo(List<RegistroHospitalario> registros) {
        for (RegistroHospitalario registro : registros) {
            ArchivoCargado archivo = registro.getArchivoCargado();
            String estado = archivo == null || archivo.getEstadoValidacion() == null
                    ? "" : archivo.getEstadoValidacion().trim().toUpperCase(Locale.ROOT);
            if (!ESTADOS_ARCHIVO_VALIDOS.contains(estado)) {
                String codigo = estado.contains("PENDIENTE")
                        ? "CALIDAD_PENDIENTE" : "CALIDAD_INVALIDA";
                throw error(HttpStatus.UNPROCESSABLE_CONTENT, codigo,
                        "Un mes requerido no procede de un archivo validado para inferencia.");
            }
        }
    }

    private boolean existeHuecoIntermedio(
            List<YearMonth> requeridos,
            Map<YearMonth, RegistroHospitalario> disponibles,
            List<YearMonth> faltantes
    ) {
        for (YearMonth faltante : faltantes) {
            int posicion = requeridos.indexOf(faltante);
            boolean hayAnterior = requeridos.subList(0, posicion).stream().anyMatch(disponibles::containsKey);
            boolean hayPosterior = requeridos.subList(posicion + 1, requeridos.size()).stream()
                    .anyMatch(disponibles::containsKey);
            if (hayAnterior && hayPosterior) return true;
        }
        return false;
    }

    private List<YearMonth> periodosRequeridos(YearMonth actual) {
        List<YearMonth> periodos = new ArrayList<>();
        for (int mesesAtras = MESES_REQUERIDOS - 1; mesesAtras >= 0; mesesAtras--) {
            periodos.add(actual.minusMonths(mesesAtras));
        }
        return periodos;
    }

    private ModeloDatosHospitalariosDTO convertir(RegistroHospitalario registro) {
        ArchivoCargado archivo = registro.getArchivoCargado();
        Ipress ipress = archivo == null ? null : archivo.getIpress();
        if (ipress == null) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "DATOS_INCOMPLETOS",
                    "Un registro requerido no tiene IPRESS asociada.");
        }
        YearMonth periodo = periodo(registro);
        ModeloDatosHospitalariosDTO datos = new ModeloDatosHospitalariosDTO();
        datos.setAnio(periodo.getYear());
        datos.setMes(periodo.getMonthValue());
        datos.setUbigeo(textoBase(ipress.getCodigoUbigeo(), "codigo_ubigeo"));
        datos.setDepartamento(textoBase(ipress.getDepartamento(), "departamento"));
        datos.setProvincia(textoBase(ipress.getProvincia(), "provincia"));
        datos.setDistrito(textoBase(ipress.getDistrito(), "distrito"));
        datos.setSector(registro.getSector() == null || registro.getSector().isBlank()
                ? "MINSA" : registro.getSector().trim());
        datos.setCategoriaIpress(textoBase(ipress.getCategoriaIpress(), "categoria_ipress"));
        datos.setCodigoIpress(textoBase(ipress.getCodigoRenipress(), "codigo_ipress"));
        datos.setIdHospitalizacion(textoBase(registro.getIdHospitalizacion(), "id_hospitalizacion"));
        datos.setServicioHospitalizacion(textoBase(registro.getServicioHospitalario(), "servicio_hospitalizacion"));
        datos.setTotalIngresos(numeroBase(registro.getIngresos(), "ingresos"));
        datos.setTotalEgresos(numeroBase(registro.getEgresos(), "egresos"));
        datos.setTotalEstancias(numeroBase(registro.getEstancias(), "estancias"));
        datos.setTotalPacientesCamas(numeroBase(registro.getPacientesCama(), "pacientes_cama"));
        datos.setTotalCamas(numeroBase(registro.getCamasTotales(), "camas_totales"));
        if (registro.getTotalCamasDisponibles() != null) {
            datos.setTotalCamasDisponibles(numeroBase(
                    registro.getTotalCamasDisponibles(), "total_camas_disponibles"));
        } else {
            datos.setTotalCamasDisponibles(
                    numeroBase(registro.getCamasDisponiblesHabilitadas(), "camas_disponibles_habilitadas")
                            * periodo.lengthOfMonth()
            );
        }
        datos.setTotalFallecidos(registro.getFallecidos() == null
                ? 0.0 : numeroBase(registro.getFallecidos(), "fallecidos"));
        return datos;
    }

    private void validarTrazabilidad(
            ModeloPrediccionBinariaResponseDTO respuesta,
            String codigoIpress,
            String servicio,
            YearMonth periodoActual
    ) {
        if (respuesta == null
                || !codigoIpress.equalsIgnoreCase(limpio(respuesta.getCodigoIpress()))
                || !servicio.equalsIgnoreCase(limpio(respuesta.getServicioHospitalizacion()))
                || !periodoActual.toString().equals(respuesta.getPeriodoActual())
                || !periodoActual.plusMonths(1).toString().equals(respuesta.getPeriodoPredicho())) {
            throw error(HttpStatus.BAD_GATEWAY, "RESPUESTA_MODELO_INCONSISTENTE",
                    "La trazabilidad devuelta por FastAPI no coincide con la solicitud validada.");
        }
    }

    private YearMonth periodo(RegistroHospitalario registro) {
        try {
            return YearMonth.of(registro.getAnio(), registro.getMes());
        } catch (RuntimeException error) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "DATOS_INCOMPLETOS",
                    "Un registro contiene un periodo invalido.");
        }
    }

    private YearMonth periodoRequerido(String valor) {
        try {
            if (valor == null || !valor.trim().matches("\\d{4}-(0[1-9]|1[0-2])")) throw new DateTimeParseException("", "", 0);
            return YearMonth.parse(valor.trim());
        } catch (DateTimeParseException error) {
            throw error(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                    "periodo_actual debe tener formato YYYY-MM.");
        }
    }

    private String textoRequerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw error(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                    "El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private String textoBase(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "DATOS_INCOMPLETOS",
                    "El campo " + campo + " esta incompleto en la base de datos.");
        }
        return valor.trim();
    }

    private Double numeroBase(Integer valor, String campo) {
        if (valor == null || valor < 0) {
            throw error(HttpStatus.UNPROCESSABLE_CONTENT, "DATOS_INCOMPLETOS",
                    "El campo " + campo + " esta ausente o es invalido en la base de datos.");
        }
        return valor.doubleValue();
    }

    private String limpio(String valor) { return valor == null ? "" : valor.trim(); }

    private PrediccionBinariaAutomaticaException error(HttpStatus status, String codigo, String mensaje) {
        return new PrediccionBinariaAutomaticaException(status, codigo, mensaje);
    }
}
