package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Reporte;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IReporteRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImplements implements IReporteInterfaces {

    private static final String ROL_ADMISION = "ADMISION_REGISTROS";
    private static final String ROL_HOSPITALIZACION = "ATENCION_HOSPITALIZACION";

    @Autowired
    private IReporteRepository rR;

    @Autowired
    private IUsuarioRepository uR;

    @Autowired
    private IPrediccionRiesgoRepository pR;

    @Override
    @Transactional
    public void generarPorArchivo(Long idArchivo, Long idUsuario) {

        Usuario usuario = uR.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        validarUsuarioAdmision(usuario);

        Long idIpress = usuario.getIpress().getIdIpress();

        List<PrediccionRiesgo> predicciones = pR.findAll().stream()
                .filter(prediccion -> prediccionPerteneceAIpress(prediccion, idIpress))
                .filter(prediccion -> idArchivo.equals(obtenerIdArchivo(prediccion)))
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
            reporte.setUsuarioGenerador(usuario.getNombre());
            reporte.setRutaArchivo(generarRutaSimulada(prediccion));

            rR.save(reporte);
        }
    }

    @Override
    @Transactional
    public void generarReporte(String correoUsuario, ReporteDTO dto) {

        Usuario usuario = obtenerUsuarioHospitalizacion(correoUsuario);
        Long idIpress = usuario.getIpress().getIdIpress();

        PrediccionRiesgo prediccion = pR.findById(dto.getIdPrediccion())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Prediccion de riesgo no encontrada"
                ));

        if (!prediccionPerteneceAIpress(prediccion, idIpress)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Prediccion de riesgo no encontrada"
            );
        }

        Reporte reporte = rR.findByPrediccionRiesgo_IdPrediccion(dto.getIdPrediccion())
                .orElse(new Reporte());

        reporte.setPrediccionRiesgo(prediccion);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setUsuarioGenerador(usuario.getNombre());
        reporte.setRutaArchivo(generarRutaSimulada(prediccion));

        rR.save(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteListDTO> list(String correoUsuario) {
        return obtenerReportesPermitidos(correoUsuario).stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteListDTO listId(String correoUsuario, Integer idReporte) {

        Long idIpress = obtenerUsuarioHospitalizacion(correoUsuario)
                .getIpress()
                .getIdIpress();

        Reporte reporte = rR.findById(idReporte)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reporte no encontrado"
                ));

        validarReportePermitido(reporte, idIpress);

        return convertToListDTO(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteListDTO listByPrediccion(String correoUsuario, Integer idPrediccion) {

        Long idIpress = obtenerUsuarioHospitalizacion(correoUsuario)
                .getIpress()
                .getIdIpress();

        Reporte reporte = rR.findByPrediccionRiesgo_IdPrediccion(idPrediccion)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe reporte para esta prediccion"
                ));

        validarReportePermitido(reporte, idIpress);

        return convertToListDTO(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteListDTO> listByArchivo(String correoUsuario, Long idArchivo) {
        return obtenerReportesPermitidos(correoUsuario).stream()
                .filter(reporte -> idArchivo.equals(obtenerIdArchivo(reporte.getPrediccionRiesgo())))
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(String correoUsuario, Integer idReporte) {

        Long idIpress = obtenerUsuarioHospitalizacion(correoUsuario)
                .getIpress()
                .getIdIpress();

        Reporte reporte = rR.findById(idReporte)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reporte no encontrado"
                ));

        validarReportePermitido(reporte, idIpress);

        rR.delete(reporte);
    }

    private List<Reporte> obtenerReportesPermitidos(String correoUsuario) {

        Usuario usuario = obtenerUsuarioHospitalizacion(correoUsuario);
        Long idIpress = usuario.getIpress().getIdIpress();

        return rR.findAll().stream()
                .filter(reporte -> reportePerteneceAIpress(reporte, idIpress))
                .collect(Collectors.toList());
    }

    private Usuario obtenerUsuarioHospitalizacion(String correoUsuario) {

        if (correoUsuario == null || correoUsuario.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario autenticado no encontrado"
            );
        }

        Usuario usuario = uR.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"
                ));

        validarUsuarioHospitalizacion(usuario);

        return usuario;
    }

    private void validarUsuarioHospitalizacion(Usuario usuario) {

        validarRolAsignado(usuario);

        if (!ROL_HOSPITALIZACION.equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permiso para consultar reportes hospitalarios"
            );
        }

        validarIpressAsignada(usuario);
    }

    private void validarUsuarioAdmision(Usuario usuario) {

        validarRolAsignado(usuario);

        if (!ROL_ADMISION.equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permiso para generar reportes desde una carga"
            );
        }

        validarIpressAsignada(usuario);
    }

    private void validarRolAsignado(Usuario usuario) {

        if (usuario.getRol() == null
                || usuario.getRol().getNombreRol() == null
                || usuario.getRol().getNombreRol().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene un rol asignado"
            );
        }
    }

    private void validarIpressAsignada(Usuario usuario) {

        if (usuario.getIpress() == null || usuario.getIpress().getIdIpress() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene una IPRESS asignada"
            );
        }
    }

    private void validarReportePermitido(Reporte reporte, Long idIpress) {

        if (!reportePerteneceAIpress(reporte, idIpress)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Reporte no encontrado"
            );
        }
    }

    private boolean reportePerteneceAIpress(Reporte reporte, Long idIpress) {

        return reporte != null
                && prediccionPerteneceAIpress(reporte.getPrediccionRiesgo(), idIpress);
    }

    private boolean prediccionPerteneceAIpress(PrediccionRiesgo prediccion, Long idIpress) {

        if (prediccion == null
                || idIpress == null
                || prediccion.getIndicadorHospitalario() == null
                || prediccion.getIndicadorHospitalario().getRegistroHospitalario() == null
                || prediccion.getIndicadorHospitalario().getRegistroHospitalario().getArchivoCargado() == null
                || prediccion.getIndicadorHospitalario().getRegistroHospitalario().getArchivoCargado().getIpress() == null) {
            return false;
        }

        return idIpress.equals(
                prediccion.getIndicadorHospitalario()
                        .getRegistroHospitalario()
                        .getArchivoCargado()
                        .getIpress()
                        .getIdIpress()
        );
    }

    private Long obtenerIdArchivo(PrediccionRiesgo prediccion) {

        if (prediccion == null
                || prediccion.getIndicadorHospitalario() == null
                || prediccion.getIndicadorHospitalario().getRegistroHospitalario() == null
                || prediccion.getIndicadorHospitalario().getRegistroHospitalario().getArchivoCargado() == null) {
            return null;
        }

        return prediccion.getIndicadorHospitalario()
                .getRegistroHospitalario()
                .getArchivoCargado()
                .getIdArchivo();
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
