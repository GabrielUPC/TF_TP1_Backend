package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionRiesgoListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IIndicadorHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrediccionRiesgoServiceImplements implements IPrediccionRiesgoInterfaces {

    @Autowired
    private IPrediccionRiesgoRepository pR;

    @Autowired
    private IIndicadorHospitalarioRepository iR;

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

        Double probabilidad = calcularProbabilidad(indicador);
        String nivelRiesgo = clasificarRiesgo(probabilidad);

        prediccion.setProbabilidad(probabilidad);
        prediccion.setNivelRiesgo(nivelRiesgo);
        prediccion.setModeloUtilizado("REGLAS_PROTOTIPO_TEMPORAL");
        prediccion.setFechaPrediccion(LocalDateTime.now());

        pR.save(prediccion);
    }

    private Double calcularProbabilidad(IndicadorHospitalario indicador) {

        double ocupacion = obtenerValor(indicador.getOcupacionEstimada());
        double presion = obtenerValor(indicador.getPresionIngresosCamas());
        double promedioEstancia = obtenerValor(indicador.getPromedioEstancia());
        double rotacion = obtenerValor(indicador.getRotacionCamas());

        double score = 0.0;

        if (ocupacion >= 1.00) {
            score += 0.45;
        } else if (ocupacion >= 0.85) {
            score += 0.30;
        } else {
            score += 0.15;
        }

        if (presion >= 1.20) {
            score += 0.30;
        } else if (presion >= 1.00) {
            score += 0.20;
        } else {
            score += 0.10;
        }

        if (promedioEstancia >= 7.00) {
            score += 0.15;
        } else if (promedioEstancia >= 5.00) {
            score += 0.10;
        } else {
            score += 0.05;
        }

        if (rotacion >= 1.20) {
            score += 0.10;
        } else if (rotacion >= 1.00) {
            score += 0.07;
        } else {
            score += 0.03;
        }

        if (score > 0.99) {
            score = 0.99;
        }

        return redondear(score);
    }

    private String clasificarRiesgo(Double probabilidad) {

        if (probabilidad >= 0.70) {
            return "ALTO";
        } else if (probabilidad >= 0.45) {
            return "MEDIO";
        } else {
            return "BAJO";
        }
    }

    private Double obtenerValor(Double valor) {
        return valor == null ? 0.0 : valor;
    }

    private Double redondear(Double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private PrediccionRiesgoListDTO convertToListDTO(PrediccionRiesgo prediccion) {

        PrediccionRiesgoListDTO dto = new PrediccionRiesgoListDTO();

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

        return dto;
    }
}