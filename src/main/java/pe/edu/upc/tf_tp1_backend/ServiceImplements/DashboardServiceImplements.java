package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.NormalizacionPredictiva;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardDetalleDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardResumenDTO;
import pe.edu.upc.tf_tp1_backend.Entities.*;
import pe.edu.upc.tf_tp1_backend.Repositories.*;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IDashboardInterfaces;

import java.util.*;
import java.util.function.ToIntFunction;

@Service
public class DashboardServiceImplements implements IDashboardInterfaces {
    private static final String ROL_HOSPITALIZACION = "ATENCION_HOSPITALIZACION";
    private static final String MODELO = PrediccionBinariaAutomaticaService.MODELO;
    private final IIndicadorHospitalarioRepository indicadorRepository;
    private final IPrediccionRiesgoRepository prediccionRepository;
    private final IUsuarioRepository usuarioRepository;

    public DashboardServiceImplements(
            IIndicadorHospitalarioRepository indicadorRepository,
            IPrediccionRiesgoRepository prediccionRepository,
            IUsuarioRepository usuarioRepository
    ) {
        this.indicadorRepository = indicadorRepository;
        this.prediccionRepository = prediccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override @Transactional(readOnly = true)
    public DashboardResumenDTO obtenerResumenGeneral(String correo, Long idArchivo) {
        return resumen(detallePermitido(correo).stream()
                .filter(d -> idArchivo == null || idArchivo.equals(d.getIdArchivo())).toList());
    }

    @Override @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> obtenerDetalleGeneral(String correo, Long idArchivo) {
        return detallePermitido(correo).stream()
                .filter(d -> idArchivo == null || idArchivo.equals(d.getIdArchivo())).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> obtenerDetallePorArchivo(String correo, Long idArchivo) {
        return obtenerDetalleGeneral(correo, idArchivo);
    }

    @Override @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> obtenerDetallePorRiesgo(String correo, String nivel) {
        String n = NormalizacionPredictiva.texto(nivel);
        return detallePermitido(correo).stream()
                .filter(d -> n.equals(d.getNivelRiesgo())).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> filtrar(
            String correo, Long idArchivo, Integer anio, Integer mes, String servicio
    ) {
        String canonico = servicio == null ? "" : NormalizacionPredictiva.texto(servicio);
        return detallePermitido(correo).stream()
                .filter(d -> idArchivo == null || idArchivo.equals(d.getIdArchivo()))
                .filter(d -> anio == null || anio.equals(d.getAnio()))
                .filter(d -> mes == null || mes.equals(d.getMes()))
                .filter(d -> canonico.isBlank() || canonico.equals(
                        NormalizacionPredictiva.texto(d.getServicioHospitalario())))
                .toList();
    }

    @Override @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> obtenerAlertas(String correo, Long idArchivo) {
        Map<String, DashboardDetalleDTO> unicas = new LinkedHashMap<>();
        obtenerDetalleGeneral(correo, idArchivo).stream()
                .filter(d -> Boolean.TRUE.equals(d.getAlertaAltaPresion()))
                .forEach(d -> unicas.put(
                        NormalizacionPredictiva.texto(d.getServicioHospitalario()) + "|"
                                + d.getPeriodoPredicho() + "|" + d.getModeloUtilizado(), d));
        return List.copyOf(unicas.values());
    }

    private List<DashboardDetalleDTO> detallePermitido(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        String rol = usuario.getRol() == null ? ""
                : NormalizacionPredictiva.texto(usuario.getRol().getNombreRol());
        if (!Boolean.TRUE.equals(usuario.getEstado())
                || !ROL_HOSPITALIZACION.equals(rol) || usuario.getIpress() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El usuario no tiene una IPRESS hospitalaria habilitada");
        }
        return indicadorRepository.buscarPorIpress(usuario.getIpress().getIdIpress()).stream()
                .map(this::convertir).toList();
    }

    private DashboardDetalleDTO convertir(IndicadorHospitalario i) {
        RegistroHospitalario r = i.getRegistroHospitalario();
        ArchivoCargado a = r.getArchivoCargado();
        DashboardDetalleDTO d = new DashboardDetalleDTO();
        d.setIdIndicador(i.getIdIndicador()); d.setIdRegistro(r.getIdRegistro());
        d.setIdArchivo(a.getIdArchivo()); d.setNombreArchivo(a.getNombreArchivo());
        d.setAnio(r.getAnio()); d.setMes(r.getMes());
        d.setServicioHospitalario(r.getServicioHospitalario());
        d.setCodigoIpress(a.getIpress().getCodigoRenipress());
        d.setIngresos(r.getIngresos()); d.setEgresos(r.getEgresos());
        d.setEstancias(r.getEstancias()); d.setPacientesCama(r.getPacientesCama());
        d.setCamasTotales(r.getCamasTotales());
        d.setCamasDisponiblesHabilitadas(r.getCamasDisponiblesHabilitadas());
        d.setTotalCamasDisponibles(r.getTotalCamasDisponibles());
        d.setOcupacionEstimada(i.getOcupacionEstimada());
        d.setPresionIngresosCamas(i.getPresionIngresosCamas());
        d.setPromedioEstancia(i.getPromedioEstancia()); d.setRotacionCamas(i.getRotacionCamas());
        prediccionRepository.findByIndicadorHospitalario_IdIndicador(i.getIdIndicador())
                .filter(p -> Boolean.TRUE.equals(p.getVigente()) && MODELO.equals(p.getModeloUtilizado()))
                .ifPresent(p -> {
                    d.setIdPrediccion(p.getIdPrediccion());
                    d.setProbabilidadAltaPresion(p.getProbabilidadAltaPresion());
                    d.setThreshold(p.getThreshold());
                    d.setAlertaAltaPresion(p.getAlertaAltaPresion());
                    d.setNivelRiesgo(p.getNivelRiesgo());
                    d.setDescripcionRiesgo(p.getDescripcionRiesgo());
                    d.setPeriodoPredicho(p.getPeriodoPredicho());
                    d.setModeloUtilizado(p.getModeloUtilizado());
                    d.setFechaPrediccion(p.getFechaPrediccion());
                });
        return d;
    }

    private DashboardResumenDTO resumen(List<DashboardDetalleDTO> lista) {
        DashboardResumenDTO d = new DashboardResumenDTO();
        List<DashboardDetalleDTO> pred = lista.stream().filter(x -> x.getIdPrediccion() != null).toList();
        long bajo = pred.stream().filter(x -> "BAJO".equals(x.getNivelRiesgo())).count();
        long medio = pred.stream().filter(x -> "MEDIO".equals(x.getNivelRiesgo())).count();
        long alto = pred.stream().filter(x -> "ALTO".equals(x.getNivelRiesgo())).count();
        d.setTotalRegistros((long) lista.size()); d.setTotalPredicciones((long) pred.size());
        d.setTotalRiesgoBajo(bajo); d.setTotalRiesgoMedio(medio); d.setTotalRiesgoAlto(alto);
        d.setPromedioOcupacionEstimada(promedio(lista.stream().map(DashboardDetalleDTO::getOcupacionEstimada).toList()));
        d.setPromedioPresionIngresosCamas(promedio(lista.stream().map(DashboardDetalleDTO::getPresionIngresosCamas).toList()));
        d.setPromedioProbabilidadAltaPresion(promedio(pred.stream().map(DashboardDetalleDTO::getProbabilidadAltaPresion).toList()));
        d.setTotalIngresos(suma(lista, x -> cero(x.getIngresos())));
        d.setTotalEgresos(suma(lista, x -> cero(x.getEgresos())));
        d.setTotalEstancias(suma(lista, x -> cero(x.getEstancias())));
        d.setTotalPacientesCama(suma(lista, x -> cero(x.getPacientesCama())));
        d.setTotalCamasDisponiblesHabilitadas(suma(lista, x -> cero(x.getCamasDisponiblesHabilitadas())));
        d.setTotalDiasCamaDisponibles(suma(lista, x -> cero(x.getTotalCamasDisponibles())));
        String predominante = alto >= medio && alto >= bajo && alto > 0 ? "ALTO"
                : medio >= bajo && medio > 0 ? "MEDIO" : bajo > 0 ? "BAJO" : "SIN DATOS";
        d.setNivelRiesgoPredominante(predominante);
        d.setMensajeResumen(lista.isEmpty() ? "No existen registros observados para los filtros seleccionados."
                : pred.isEmpty() ? "Hay datos observados; analice un periodo elegible para generar su prediccion."
                : "Resumen de datos observados y predicciones binarias vigentes.");
        return d;
    }
    private int suma(List<DashboardDetalleDTO> l, ToIntFunction<DashboardDetalleDTO> f) {
        return l.stream().mapToInt(f).sum();
    }
    private int cero(Integer v) { return v == null ? 0 : v; }
    private Double promedio(List<Double> valores) {
        return valores.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
