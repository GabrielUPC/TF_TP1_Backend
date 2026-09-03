package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionRiesgoListDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloDatosHospitalariosDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionRequestDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionResponseDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IIndicadorHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IHallazgoCalidadDatosRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.HallazgoCalidadImportado;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PrediccionRiesgoServiceImplements implements IPrediccionRiesgoInterfaces {

    @Autowired
    private IPrediccionRiesgoRepository pR;

    @Autowired
    private IIndicadorHospitalarioRepository iR;

    @Autowired
    private IRegistroHospitalarioRepository rR;

    @Autowired
    private ModeloPredictivoClientService modeloClient;
    @Autowired
    private IHallazgoCalidadDatosRepository hallazgoCalidadRepository;

    @Override
    @Transactional
    public void predecirPorIndicador(Integer idIndicador) {

        IndicadorHospitalario indicador = iR.findById(idIndicador)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Indicador hospitalario no encontrado"
                ));

        generarYGuardarPrediccion(indicador);
    }

    @Override
    @Transactional
    public void predecirPorArchivo(Long idArchivo) {

        List<IndicadorHospitalario> indicadores = iR.findByRegistroHospitalario_ArchivoCargado_IdArchivo(idArchivo);

        if (indicadores.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existen indicadores calculados para el archivo indicado"
            );
        }

        for (IndicadorHospitalario indicador : indicadores) {
            generarYGuardarPrediccion(indicador);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrediccionRiesgoListDTO> list() {
        return pR.findAll().stream()
                .filter(this::esVigente)
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PrediccionRiesgoListDTO listId(Integer idPrediccion) {

        PrediccionRiesgo prediccion = pR.findById(idPrediccion)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Predicción de riesgo no encontrada"
                ));

        return convertToListDTO(prediccion);
    }

    @Override
    @Transactional(readOnly = true)
    public PrediccionRiesgoListDTO listByIndicador(Integer idIndicador) {

        PrediccionRiesgo prediccion = pR.findByIndicadorHospitalario_IdIndicador(idIndicador)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe predicción para este indicador"
                ));

        return convertToListDTO(prediccion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrediccionRiesgoListDTO> listByArchivo(Long idArchivo) {
        return pR.findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(idArchivo).stream()
                .filter(this::esVigente)
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer idPrediccion) {

        if (!pR.existsById(idPrediccion)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Predicción de riesgo no encontrada"
            );
        }

        pR.deleteById(idPrediccion);
    }

    private void generarYGuardarPrediccion(IndicadorHospitalario indicador) {

        PrediccionRiesgo prediccion = pR.findByIndicadorHospitalario_IdIndicador(indicador.getIdIndicador())
                .orElse(new PrediccionRiesgo());

        prediccion.setIndicadorHospitalario(indicador);

        RegistroHospitalario registro = indicador.getRegistroHospitalario();
        if (registro == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "El indicador no tiene un registro hospitalario asociado"
            );
        }
        if (esPeriodoPendiente(registro)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Sin prediccion: datos pendientes de validacion Q05/Q06.");
        }

        ModeloPrediccionRequestDTO solicitud = construirSolicitud(registro);
        ModeloPrediccionResponseDTO respuesta = modeloClient.predecir(solicitud);
        // FastAPI decide la clase final. No aplicar argmax ni umbrales en Java.
        String nivelRiesgo = validarNivelRiesgo(respuesta.getNivelRiesgoPredicho());
        // Probabilidad de esa clase FINAL, que puede no ser el máximo de las tres.
        Double probabilidad = validarProbabilidad(respuesta.getProbabilidad());
        Double probabilidadRiesgoBajo = obtenerProbabilidadRespuesta(respuesta, "bajo");
        Double probabilidadRiesgoMedio = obtenerProbabilidadRespuesta(respuesta, "medio");
        Double probabilidadRiesgoAlto = obtenerProbabilidadRespuesta(respuesta, "alto");
        // Índice visual/operativo legado, no probabilidad calibrada ni criterio de clasificación.
        Double riesgoInsuficienciaCapacidad = respuesta.getRiesgoInsuficienciaCapacidad() == null
                ? null : validarProbabilidad(respuesta.getRiesgoInsuficienciaCapacidad());

        prediccion.setProbabilidad(probabilidad);
        prediccion.setProbabilidadRiesgoBajo(probabilidadRiesgoBajo);
        prediccion.setProbabilidadRiesgoMedio(probabilidadRiesgoMedio);
        prediccion.setProbabilidadRiesgoAlto(probabilidadRiesgoAlto);
        prediccion.setRiesgoInsuficienciaCapacidad(riesgoInsuficienciaCapacidad);
        prediccion.setNivelRiesgo(nivelRiesgo);
        prediccion.setModeloUtilizado("XGBoost - FastAPI");
        prediccion.setFechaPrediccion(LocalDateTime.now());
        prediccion.setVigente(true);
        prediccion.setMotivoNoVigente(null);

        pR.save(prediccion);
    }

    private ModeloPrediccionRequestDTO construirSolicitud(RegistroHospitalario registroActual) {
        ModeloPrediccionRequestDTO solicitud = new ModeloPrediccionRequestDTO();
        solicitud.setRegistroActual(convertirDatosModelo(registroActual));
        solicitud.setHistorialUltimosMeses(
                obtenerHistorial(registroActual).stream()
                        .map(this::convertirDatosModelo)
                        .collect(Collectors.toList())
        );
        return solicitud;
    }

    private List<RegistroHospitalario> obtenerHistorial(RegistroHospitalario registroActual) {
        ArchivoCargado archivo = registroActual.getArchivoCargado();
        Ipress ipress = archivo == null ? null : archivo.getIpress();
        if (ipress == null || ipress.getIdIpress() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "El registro hospitalario no tiene una IPRESS asociada"
            );
        }

        YearMonth periodoActual = obtenerPeriodo(registroActual);
        List<YearMonth> periodosBuscados = List.of(
                periodoActual.minusMonths(2),
                periodoActual.minusMonths(1)
        );

        List<RegistroHospitalario> candidatos =
                rR.findByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(
                        ipress.getIdIpress(),
                        textoRequerido(
                                registroActual.getServicioHospitalario(),
                                "servicioHospitalario"
                        )
                );

        Map<YearMonth, RegistroHospitalario> registroPorPeriodo = new HashMap<>();
        for (RegistroHospitalario candidato : candidatos) {
            ArchivoCargado archivoCandidato = candidato.getArchivoCargado();
            Ipress ipressCandidata = archivoCandidato == null ? null : archivoCandidato.getIpress();
            if (ipressCandidata == null || !ipress.getIdIpress().equals(ipressCandidata.getIdIpress())
                    || !textoRequerido(registroActual.getServicioHospitalario(), "servicioHospitalario")
                    .equalsIgnoreCase(candidato.getServicioHospitalario() == null
                            ? "" : candidato.getServicioHospitalario().trim())) continue;
            YearMonth periodo = obtenerPeriodo(candidato);
            if (!periodosBuscados.contains(periodo)) {
                continue;
            }
            if (esPeriodoPendiente(candidato)) {
                continue;
            }

            registroPorPeriodo.merge(
                    periodo,
                    candidato,
                    this::registroMasReciente
            );
        }

        List<RegistroHospitalario> historial = new ArrayList<>();
        for (YearMonth periodo : periodosBuscados) {
            RegistroHospitalario registro = registroPorPeriodo.get(periodo);
            if (registro != null) {
                historial.add(registro);
            }
        }
        return historial;
    }

    private RegistroHospitalario registroMasReciente(
            RegistroHospitalario primero,
            RegistroHospitalario segundo
    ) {
        int idPrimero = primero.getIdRegistro() == null ? 0 : primero.getIdRegistro();
        int idSegundo = segundo.getIdRegistro() == null ? 0 : segundo.getIdRegistro();
        return idSegundo > idPrimero ? segundo : primero;
    }

    private boolean esPeriodoPendiente(RegistroHospitalario registro) {
        ArchivoCargado archivo = registro.getArchivoCargado();
        Ipress ipress = archivo == null ? null : archivo.getIpress();
        if (ipress == null || ipress.getIdIpress() == null || registro.getAnio() == null
                || registro.getMes() == null || registro.getServicioHospitalario() == null) return false;
        return hallazgoCalidadRepository
                .existsByArchivoCargado_Ipress_IdIpressAndAnioAndMesAndServicioHospitalarioIgnoreCaseAndEstado(
                        ipress.getIdIpress(), registro.getAnio(), registro.getMes(),
                        normalizar(registro.getServicioHospitalario()), "PENDIENTE_VALIDACION");
    }

    private YearMonth obtenerPeriodo(RegistroHospitalario registro) {
        if (registro.getAnio() == null || registro.getMes() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "El registro hospitalario no tiene anio y mes completos"
            );
        }
        try {
            return YearMonth.of(registro.getAnio(), registro.getMes());
        } catch (RuntimeException error) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "El registro hospitalario tiene un periodo invalido",
                    error
            );
        }
    }

    private ModeloDatosHospitalariosDTO convertirDatosModelo(RegistroHospitalario registro) {
        ArchivoCargado archivo = registro.getArchivoCargado();
        Ipress ipress = archivo == null ? null : archivo.getIpress();
        if (ipress == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "El registro hospitalario no tiene una IPRESS asociada"
            );
        }

        YearMonth periodo = obtenerPeriodo(registro);
        ModeloDatosHospitalariosDTO datos = new ModeloDatosHospitalariosDTO();
        datos.setAnio(periodo.getYear());
        datos.setMes(periodo.getMonthValue());
        datos.setUbigeo(textoRequerido(ipress.getCodigoUbigeo(), "codigoUbigeo"));
        datos.setDepartamento(textoRequerido(ipress.getDepartamento(), "departamento"));
        datos.setProvincia(textoRequerido(ipress.getProvincia(), "provincia"));
        datos.setDistrito(textoRequerido(ipress.getDistrito(), "distrito"));
        datos.setSector(textoOpcional(registro.getSector(), "MINSA"));
        datos.setCategoriaIpress(textoRequerido(ipress.getCategoriaIpress(), "categoriaIpress"));
        datos.setCodigoIpress(textoRequerido(ipress.getCodigoRenipress(), "codigoRenipress"));

        String servicio = textoRequerido(
                registro.getServicioHospitalario(),
                "servicioHospitalario"
        );
        datos.setIdHospitalizacion(
                textoOpcional(registro.getIdHospitalizacion(), servicio)
        );
        datos.setServicioHospitalizacion(servicio);
        datos.setTotalIngresos(numeroRequerido(registro.getIngresos(), "ingresos"));
        datos.setTotalEgresos(numeroRequerido(registro.getEgresos(), "egresos"));
        datos.setTotalEstancias(numeroRequerido(registro.getEstancias(), "estancias"));
        datos.setTotalPacientesCamas(
                numeroRequerido(registro.getPacientesCama(), "pacientesCama")
        );
        datos.setTotalCamas(numeroRequerido(registro.getCamasTotales(), "camasTotales"));
        if (registro.getTotalCamasDisponibles() != null) {
            // D1: total mensual de días-cama disponibles; no camas libres.
            datos.setTotalCamasDisponibles(numeroRequerido(
                    registro.getTotalCamasDisponibles(),
                    "totalCamasDisponibles"
            ));
        } else {
            // Formato interno: camas habilitadas (número físico) por días del mes.
            double camasDisponiblesHabilitadas = numeroRequerido(
                    registro.getCamasDisponiblesHabilitadas(),
                    "camasDisponiblesHabilitadas"
            );
            datos.setTotalCamasDisponibles(
                    camasDisponiblesHabilitadas * periodo.lengthOfMonth()
            );
        }
        datos.setTotalFallecidos(
                registro.getFallecidos() == null
                        ? 0.0
                        : numeroRequerido(registro.getFallecidos(), "fallecidos")
        );
        return datos;
    }

    private String textoOpcional(String valor, String valorPredeterminado) {
        return valor == null || valor.isBlank()
                ? valorPredeterminado
                : valor.trim();
    }

    private String textoRequerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "El campo " + campo + " es obligatorio para consultar el modelo"
            );
        }
        return valor.trim();
    }

    private Double numeroRequerido(Integer valor, String campo) {
        if (valor == null || valor < 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "El campo " + campo + " debe ser un numero no negativo"
            );
        }
        return valor.doubleValue();
    }

    private String validarNivelRiesgo(String nivelRiesgo) {
        if (nivelRiesgo == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El microservicio del modelo no devolvio nivel_riesgo_predicho"
            );
        }

        String normalizado = nivelRiesgo.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("BAJO", "MEDIO", "ALTO").contains(normalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El microservicio del modelo devolvio un nivel de riesgo invalido"
            );
        }
        return normalizado;
    }

    private Double validarProbabilidad(Double probabilidad) {
        if (probabilidad == null
                || probabilidad.isNaN()
                || probabilidad < 0.0
                || probabilidad > 1.0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El microservicio del modelo devolvio una probabilidad invalida"
            );
        }
        return probabilidad;
    }
    private Double obtenerProbabilidadClase(
            Map<String, Double> probabilidadesPorClase,
            String clase
    ) {
        if (probabilidadesPorClase == null || probabilidadesPorClase.isEmpty()) {
            return null; // Ausente no significa probabilidad cero.
        }

        for (Map.Entry<String, Double> entrada : probabilidadesPorClase.entrySet()) {
            if (entrada.getKey() != null
                    && entrada.getKey().equalsIgnoreCase(clase)) {
                return validarProbabilidad(entrada.getValue());
            }
        }

        return null;
    }

    private Double obtenerProbabilidadRespuesta(
            ModeloPrediccionResponseDTO respuesta,
            String clase
    ) {
        String claseNormalizada = clase.toLowerCase(Locale.ROOT);
        Double valorExplicito = null;
        if ("bajo".equals(claseNormalizada)) {
            valorExplicito = respuesta.getProbabilidadRiesgoBajo();
        } else if ("medio".equals(claseNormalizada)) {
            valorExplicito = respuesta.getProbabilidadRiesgoMedio();
        } else if ("alto".equals(claseNormalizada)) {
            valorExplicito = respuesta.getProbabilidadRiesgoAlto();
        }

        if (valorExplicito != null) {
            return validarProbabilidad(valorExplicito);
        }

        return obtenerProbabilidadClase(respuesta.getProbabilidadesPorClase(), clase);
    }
    private PrediccionRiesgoListDTO convertToListDTO(PrediccionRiesgo prediccion) {

        PrediccionRiesgoListDTO dto = new PrediccionRiesgoListDTO();

        dto.setIdPrediccion(prediccion.getIdPrediccion());
        dto.setNivelRiesgo(prediccion.getNivelRiesgo());
        dto.setProbabilidad(prediccion.getProbabilidad());
        dto.setModeloUtilizado(prediccion.getModeloUtilizado());
        dto.setFechaPrediccion(prediccion.getFechaPrediccion());
        dto.setVigente(esVigente(prediccion));
        dto.setMotivoNoVigente(prediccion.getMotivoNoVigente());

        IndicadorHospitalario indicador = prediccion.getIndicadorHospitalario();

        if (indicador != null) {
            dto.setIdIndicador(indicador.getIdIndicador());
            dto.setOcupacionEstimada(indicador.getOcupacionEstimada());
            dto.setPresionIngresosCamas(indicador.getPresionIngresosCamas());
            dto.setPromedioEstancia(indicador.getPromedioEstancia());
            dto.setRotacionCamas(indicador.getRotacionCamas());

            RegistroHospitalario registro = indicador.getRegistroHospitalario();

            if (registro != null) {
                dto.setIdRegistro(registro.getIdRegistro());
                dto.setAnio(registro.getAnio());
                dto.setMes(registro.getMes());
                completarPeriodoPredicho(dto, registro);
                dto.setServicioHospitalario(registro.getServicioHospitalario());
                dto.setIngresos(registro.getIngresos());
                dto.setEgresos(registro.getEgresos());
                dto.setEstancias(registro.getEstancias());
                dto.setPacientesCama(registro.getPacientesCama());
                dto.setCamasTotales(registro.getCamasTotales());
                dto.setCamasDisponiblesHabilitadas(registro.getCamasDisponiblesHabilitadas());

                ArchivoCargado archivo = registro.getArchivoCargado();

                if (archivo != null) {
                    dto.setIdArchivo(archivo.getIdArchivo());
                    dto.setNombreArchivo(archivo.getNombreArchivo());
                    if (archivo.getIpress() != null) {
                        dto.setCodigoIpress(
                                archivo.getIpress().getCodigoRenipress()
                        );
                    }
                }
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public void invalidarPorPendientes(List<HallazgoCalidadImportado> pendientes) {
        if (pendientes == null || pendientes.isEmpty()) return;
        List<PrediccionRiesgo> cambiadas = pR.findAll().stream().filter(p -> coincidePendiente(p, pendientes))
                .peek(p -> { p.setVigente(false); p.setMotivoNoVigente("Sin prediccion vigente: datos pendientes de validacion Q05/Q06."); })
                .toList();
        if (!cambiadas.isEmpty()) pR.saveAll(cambiadas);
    }

    private boolean esVigente(PrediccionRiesgo p) { return !Boolean.FALSE.equals(p.getVigente()); }

    private boolean coincidePendiente(PrediccionRiesgo prediccion, List<HallazgoCalidadImportado> pendientes) {
        RegistroHospitalario r = prediccion.getIndicadorHospitalario() == null ? null : prediccion.getIndicadorHospitalario().getRegistroHospitalario();
        if (r == null || r.getArchivoCargado() == null || r.getArchivoCargado().getIpress() == null) return false;
        String codigo = r.getArchivoCargado().getIpress().getCodigoRenipress();
        YearMonth actual = obtenerPeriodo(r);
        return pendientes.stream().anyMatch(h -> normalizar(h.codigoIpress()).equals(normalizar(codigo))
                && normalizar(h.servicioHospitalario()).equals(normalizar(r.getServicioHospitalario()))
                && (YearMonth.of(h.anio(),h.mes()).equals(actual) || YearMonth.of(h.anio(),h.mes()).equals(actual.plusMonths(1))));
    }

    private String normalizar(String v) { return v == null ? "" : v.trim().replaceAll("\\s+"," ").toUpperCase(Locale.ROOT); }

    private void completarPeriodoPredicho(
            PrediccionRiesgoListDTO dto,
            RegistroHospitalario registro
    ) {
        if (registro.getAnio() == null || registro.getMes() == null) {
            return;
        }

        try {
            YearMonth periodoPredicho = YearMonth.of(
                    registro.getAnio(),
                    registro.getMes()
            ).plusMonths(1);
            dto.setAnioPredicho(periodoPredicho.getYear());
            dto.setMesPredicho(periodoPredicho.getMonthValue());
        } catch (RuntimeException ignored) {
            // El periodo base se conserva y el periodo predicho queda sin informar.
        }
    }
}
