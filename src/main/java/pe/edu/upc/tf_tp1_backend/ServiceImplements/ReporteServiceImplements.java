package pe.edu.upc.tf_tp1_backend.ServiceImplements;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.*;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IReporteRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImplements implements IReporteInterfaces {

    @Autowired
    private IReporteRepository rR;
    @Autowired
    private IUsuarioRepository uR;
    @Autowired
    private IPrediccionRiesgoRepository pR;

    @Override
    @Transactional
    public void generarPorArchivo(Long idArchivo, Long idUsuario) {

        Usuario usuario = null;

        if (idUsuario != null) {
            usuario = uR.findById(idUsuario).orElse(null);
        }

        String usuarioGenerador = usuario != null
                ? usuario.getNombre()
                : "Usuario no especificado";

        List<PrediccionRiesgo> predicciones = pR.findAll().stream()
                .filter(prediccion -> prediccion.getIndicadorHospitalario() != null)
                .filter(prediccion -> prediccion.getIndicadorHospitalario().getRegistroHospitalario() != null)
                .filter(prediccion -> prediccion.getIndicadorHospitalario().getRegistroHospitalario().getArchivoCargado() != null)
                .filter(prediccion -> idArchivo.equals(
                        prediccion.getIndicadorHospitalario()
                                .getRegistroHospitalario()
                                .getArchivoCargado()
                                .getIdArchivo()
                ))
                .collect(Collectors.toList());

        if (predicciones.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existen predicciones para el archivo indicado"
            );
        }

        for (PrediccionRiesgo prediccion : predicciones) {

            Reporte reporte = rR.findByPrediccionRiesgo_IdPrediccion(prediccion.getIdPrediccion())
                    .orElse(new Reporte());

            reporte.setPrediccionRiesgo(prediccion);
            reporte.setFechaGeneracion(LocalDateTime.now());
            reporte.setUsuarioGenerador(usuarioGenerador);
            reporte.setRutaArchivo(generarRutaSimulada(prediccion));

            rR.save(reporte);
        }
    }
    @Override
    @Transactional
    public void generarReporte(ReporteDTO dto) {

        PrediccionRiesgo prediccion = pR.findById(dto.getIdPrediccion())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Predicción de riesgo no encontrada"
                ));

        Reporte reporte = rR.findByPrediccionRiesgo_IdPrediccion(dto.getIdPrediccion())
                .orElse(new Reporte());

        reporte.setPrediccionRiesgo(prediccion);
        reporte.setFechaGeneracion(LocalDateTime.now());

        if (dto.getUsuarioGenerador() == null || dto.getUsuarioGenerador().isBlank()) {
            reporte.setUsuarioGenerador("Usuario no especificado");
        } else {
            reporte.setUsuarioGenerador(dto.getUsuarioGenerador());
        }

        String ruta = generarRutaSimulada(prediccion);
        reporte.setRutaArchivo(ruta);

        rR.save(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteListDTO> list() {
        return rR.findAll().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteListDTO listId(Integer idReporte) {

        Reporte reporte = rR.findById(idReporte)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reporte no encontrado"
                ));

        return convertToListDTO(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteListDTO listByPrediccion(Integer idPrediccion) {

        Reporte reporte = rR.findByPrediccionRiesgo_IdPrediccion(idPrediccion)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe reporte para esta predicción"
                ));

        return convertToListDTO(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteListDTO> listByArchivo(Long idArchivo) {
        return rR.findByPrediccionRiesgo_IndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(idArchivo).stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer idReporte) {

        if (!rR.existsById(idReporte)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Reporte no encontrado"
            );
        }

        rR.deleteById(idReporte);
    }

    private String generarRutaSimulada(PrediccionRiesgo prediccion) {

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        return "reportes/reporte_prediccion_" + prediccion.getIdPrediccion() + "_" + fecha + ".pdf";
    }

    private ReporteListDTO convertToListDTO(Reporte reporte) {

        ReporteListDTO dto = new ReporteListDTO();

        dto.setIdReporte(reporte.getIdReporte());
        dto.setFechaGeneracion(reporte.getFechaGeneracion());
        dto.setUsuarioGenerador(reporte.getUsuarioGenerador());
        dto.setRutaArchivo(reporte.getRutaArchivo());

        PrediccionRiesgo prediccion = reporte.getPrediccionRiesgo();

        if (prediccion != null) {
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

                    ArchivoCargado archivo = registro.getArchivoCargado();

                    if (archivo != null) {
                        dto.setIdArchivo(archivo.getIdArchivo());
                        dto.setNombreArchivo(archivo.getNombreArchivo());
                    }
                }
            }
        }

        return dto;
    }
}