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
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.HallazgoCalidadImportado;

import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PrediccionRiesgoServiceImplements implements IPrediccionRiesgoInterfaces {

    @Autowired
    private IPrediccionRiesgoRepository pR;

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
                .peek(p -> { p.setVigente(false); p.setMotivoNoVigente("Sin prediccion vigente: datos pendientes de validacion Q05/Q06/Q07/Q08."); })
                .toList();
        if (!cambiadas.isEmpty()) pR.saveAll(cambiadas);
    }

    private boolean esVigente(PrediccionRiesgo p) { return !Boolean.FALSE.equals(p.getVigente()); }

    private boolean coincidePendiente(PrediccionRiesgo prediccion, List<HallazgoCalidadImportado> pendientes) {
        RegistroHospitalario r = prediccion.getIndicadorHospitalario() == null ? null : prediccion.getIndicadorHospitalario().getRegistroHospitalario();
        if (r == null || r.getArchivoCargado() == null || r.getArchivoCargado().getIpress() == null) return false;
        String codigo = r.getArchivoCargado().getIpress().getCodigoRenipress();
        YearMonth actual = obtenerPeriodo(r);
        return pendientes.stream().anyMatch(h -> {
            if (!normalizar(h.codigoIpress()).equals(normalizar(codigo))
                    || !normalizar(h.servicioHospitalario()).equals(
                            normalizar(r.getServicioHospitalario()))) return false;
            YearMonth pendiente = YearMonth.of(h.anio(), h.mes());
            return !pendiente.isBefore(actual.minusMonths(5))
                    && !pendiente.isAfter(actual.plusMonths(1));
        });
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
