package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardDetalleDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardResumenDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IDashboardInterfaces;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImplements implements IDashboardInterfaces {

    @Autowired
    private IPrediccionRiesgoRepository pR;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenDTO obtenerResumenGeneral() {

        List<PrediccionRiesgo> predicciones = pR.findAll();

        DashboardResumenDTO dto = new DashboardResumenDTO();

        long total = predicciones.size();

        long bajo = predicciones.stream()
                .filter(p -> "BAJO".equalsIgnoreCase(p.getNivelRiesgo()))
                .count();

        long medio = predicciones.stream()
                .filter(p -> "MEDIO".equalsIgnoreCase(p.getNivelRiesgo()))
                .count();

        long alto = predicciones.stream()
                .filter(p -> "ALTO".equalsIgnoreCase(p.getNivelRiesgo()))
                .count();

        dto.setTotalPredicciones(total);
        dto.setTotalRiesgoBajo(bajo);
        dto.setTotalRiesgoMedio(medio);
        dto.setTotalRiesgoAlto(alto);

        dto.setPromedioOcupacionEstimada(redondear(promedioOcupacion(predicciones)));
        dto.setPromedioPresionIngresosCamas(redondear(promedioPresion(predicciones)));
        dto.setPromedioProbabilidad(redondear(promedioProbabilidad(predicciones)));

        dto.setTotalIngresos(sumarIngresos(predicciones));
        dto.setTotalEgresos(sumarEgresos(predicciones));
        dto.setTotalEstancias(sumarEstancias(predicciones));
        dto.setTotalPacientesCama(sumarPacientesCama(predicciones));
        dto.setTotalCamasDisponiblesHabilitadas(sumarCamasDisponibles(predicciones));

        dto.setNivelRiesgoPredominante(obtenerRiesgoPredominante(bajo, medio, alto));

        dto.setMensajeResumen(generarMensajeResumen(dto.getNivelRiesgoPredominante(), total));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> obtenerDetalleGeneral() {
        return pR.findAll().stream()
                .map(this::convertToDashboardDetalleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> obtenerDetallePorArchivo(Long idArchivo) {
        return pR.findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(idArchivo).stream()
                .map(this::convertToDashboardDetalleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> obtenerDetallePorRiesgo(String nivelRiesgo) {
        return pR.findAll().stream()
                .filter(p -> p.getNivelRiesgo() != null)
                .filter(p -> p.getNivelRiesgo().equalsIgnoreCase(nivelRiesgo))
                .map(this::convertToDashboardDetalleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> filtrar(Integer anio, Integer mes, String servicioHospitalario) {

        return pR.findAll().stream()
                .filter(p -> cumpleFiltroAnio(p, anio))
                .filter(p -> cumpleFiltroMes(p, mes))
                .filter(p -> cumpleFiltroServicio(p, servicioHospitalario))
                .map(this::convertToDashboardDetalleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardDetalleDTO> obtenerAlertas() {

        return pR.findAll().stream()
                .filter(p -> p.getNivelRiesgo() != null)
                .filter(p -> p.getNivelRiesgo().equalsIgnoreCase("MEDIO")
                        || p.getNivelRiesgo().equalsIgnoreCase("ALTO"))
                .map(this::convertToDashboardDetalleDTO)
                .collect(Collectors.toList());
    }

    private DashboardDetalleDTO convertToDashboardDetalleDTO(PrediccionRiesgo prediccion) {

        DashboardDetalleDTO dto = new DashboardDetalleDTO();

        dto.setIdPrediccion(prediccion.getIdPrediccion());
        dto.setNivelRiesgo(prediccion.getNivelRiesgo());
        dto.setProbabilidad(prediccion.getProbabilidad());
        dto.setModeloUtilizado(prediccion.getModeloUtilizado());
        dto.setFechaPrediccion(prediccion.getFechaPrediccion());

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
                }
            }
        }

        dto.setAlerta(generarAlerta(prediccion.getNivelRiesgo()));
        dto.setInterpretacion(generarInterpretacion(prediccion));

        return dto;
    }

    private String generarAlerta(String nivelRiesgo) {

        if (nivelRiesgo == null) {
            return "Sin alerta";
        }

        if (nivelRiesgo.equalsIgnoreCase("ALTO")) {
            return "Alerta crítica: revisar capacidad asistencial del servicio hospitalario.";
        }

        if (nivelRiesgo.equalsIgnoreCase("MEDIO")) {
            return "Alerta preventiva: monitorear indicadores de camas y demanda hospitalaria.";
        }

        return "Sin alerta crítica.";
    }

    private String generarInterpretacion(PrediccionRiesgo prediccion) {

        if (prediccion.getNivelRiesgo() == null) {
            return "No se cuenta con nivel de riesgo calculado.";
        }

        if (prediccion.getNivelRiesgo().equalsIgnoreCase("ALTO")) {
            return "El servicio presenta alta presión asistencial según los indicadores calculados.";
        }

        if (prediccion.getNivelRiesgo().equalsIgnoreCase("MEDIO")) {
            return "El servicio presenta presión asistencial moderada y requiere seguimiento.";
        }

        return "El servicio presenta presión asistencial baja según los indicadores calculados.";
    }

    private boolean cumpleFiltroAnio(PrediccionRiesgo prediccion, Integer anio) {

        if (anio == null) {
            return true;
        }

        RegistroHospitalario registro = obtenerRegistro(prediccion);

        return registro != null && Objects.equals(registro.getAnio(), anio);
    }

    private boolean cumpleFiltroMes(PrediccionRiesgo prediccion, Integer mes) {

        if (mes == null) {
            return true;
        }

        RegistroHospitalario registro = obtenerRegistro(prediccion);

        return registro != null && Objects.equals(registro.getMes(), mes);
    }

    private boolean cumpleFiltroServicio(PrediccionRiesgo prediccion, String servicioHospitalario) {

        if (servicioHospitalario == null || servicioHospitalario.isBlank()) {
            return true;
        }

        RegistroHospitalario registro = obtenerRegistro(prediccion);

        return registro != null
                && registro.getServicioHospitalario() != null
                && registro.getServicioHospitalario().equalsIgnoreCase(servicioHospitalario);
    }

    private RegistroHospitalario obtenerRegistro(PrediccionRiesgo prediccion) {

        if (prediccion.getIndicadorHospitalario() == null) {
            return null;
        }

        return prediccion.getIndicadorHospitalario().getRegistroHospitalario();
    }

    private Double promedioOcupacion(List<PrediccionRiesgo> predicciones) {

        return predicciones.stream()
                .map(PrediccionRiesgo::getIndicadorHospitalario)
                .filter(Objects::nonNull)
                .map(IndicadorHospitalario::getOcupacionEstimada)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private Double promedioPresion(List<PrediccionRiesgo> predicciones) {

        return predicciones.stream()
                .map(PrediccionRiesgo::getIndicadorHospitalario)
                .filter(Objects::nonNull)
                .map(IndicadorHospitalario::getPresionIngresosCamas)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private Double promedioProbabilidad(List<PrediccionRiesgo> predicciones) {

        return predicciones.stream()
                .map(PrediccionRiesgo::getProbabilidad)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private Integer sumarIngresos(List<PrediccionRiesgo> predicciones) {

        return predicciones.stream()
                .map(this::obtenerRegistro)
                .filter(Objects::nonNull)
                .map(RegistroHospitalario::getIngresos)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Integer sumarEgresos(List<PrediccionRiesgo> predicciones) {

        return predicciones.stream()
                .map(this::obtenerRegistro)
                .filter(Objects::nonNull)
                .map(RegistroHospitalario::getEgresos)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Integer sumarEstancias(List<PrediccionRiesgo> predicciones) {

        return predicciones.stream()
                .map(this::obtenerRegistro)
                .filter(Objects::nonNull)
                .map(RegistroHospitalario::getEstancias)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Integer sumarPacientesCama(List<PrediccionRiesgo> predicciones) {

        return predicciones.stream()
                .map(this::obtenerRegistro)
                .filter(Objects::nonNull)
                .map(RegistroHospitalario::getPacientesCama)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Integer sumarCamasDisponibles(List<PrediccionRiesgo> predicciones) {

        return predicciones.stream()
                .map(this::obtenerRegistro)
                .filter(Objects::nonNull)
                .map(RegistroHospitalario::getCamasDisponiblesHabilitadas)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String obtenerRiesgoPredominante(long bajo, long medio, long alto) {

        if (alto >= medio && alto >= bajo) {
            return "ALTO";
        }

        if (medio >= alto && medio >= bajo) {
            return "MEDIO";
        }

        if (bajo > 0) {
            return "BAJO";
        }

        return "SIN DATOS";
    }

    private String generarMensajeResumen(String riesgoPredominante, long totalPredicciones) {

        if (totalPredicciones == 0) {
            return "No existen predicciones registradas para mostrar en el dashboard.";
        }

        if ("ALTO".equalsIgnoreCase(riesgoPredominante)) {
            return "El dashboard evidencia predominio de riesgo alto. Se recomienda revisar los servicios con mayor presión asistencial.";
        }

        if ("MEDIO".equalsIgnoreCase(riesgoPredominante)) {
            return "El dashboard evidencia predominio de riesgo medio. Se recomienda realizar seguimiento preventivo.";
        }

        if ("BAJO".equalsIgnoreCase(riesgoPredominante)) {
            return "El dashboard evidencia predominio de riesgo bajo según los indicadores procesados.";
        }

        return "No se pudo determinar un riesgo predominante.";
    }

    private Double redondear(Double valor) {

        if (valor == null) {
            return 0.0;
        }

        return Math.round(valor * 100.0) / 100.0;
    }
}