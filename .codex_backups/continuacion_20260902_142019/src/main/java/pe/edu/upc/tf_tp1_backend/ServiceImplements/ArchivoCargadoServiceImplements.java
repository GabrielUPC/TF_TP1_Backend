package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ArchivoProcesadoDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IArchivoCargadoInterfaces;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArchivoCargadoServiceImplements implements IArchivoCargadoInterfaces {

    private static final String ROL_ADMISION = "ADMISION_REGISTROS";
    private static final String ROL_HOSPITALIZACION = "ATENCION_HOSPITALIZACION";

    @Autowired
    private IArchivoCargadoRepository archivoCargadoRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IRegistroHospitalarioRepository registroHospitalarioRepository;

    @Autowired
    private IPrediccionRiesgoRepository prediccionRiesgoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ArchivoCargado> listarPorUsuarioAutenticado(String correoUsuario) {
        Usuario usuario = obtenerUsuarioAutenticado(correoUsuario);
        validarUsuarioAdmision(usuario);

        return archivoCargadoRepository.findByIpress_IdIpress(
                usuario.getIpress().getIdIpress()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArchivoProcesadoDTO> listarProcesadosPorUsuarioAutenticado(
            String correoUsuario
    ) {
        Usuario usuario = obtenerUsuarioAutenticado(correoUsuario);
        validarUsuarioConsultaProcesados(usuario);

        return archivoCargadoRepository.findByIpress_IdIpress(
                        usuario.getIpress().getIdIpress()
                ).stream()
                .filter(archivo -> "PROCESADO".equalsIgnoreCase(
                        archivo.getEstadoProcesamiento()
                ))
                .sorted(Comparator.comparing(
                        ArchivoCargado::getFechaCarga,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(this::convertirAProcesadoDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoCargado listarIdPorUsuarioAutenticado(Long idArchivo, String correoUsuario) {
        Usuario usuario = obtenerUsuarioAutenticado(correoUsuario);
        validarUsuarioAdmision(usuario);

        return archivoCargadoRepository.findByIdArchivoAndIpress_IdIpress(
                        idArchivo,
                        usuario.getIpress().getIdIpress()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Archivo no encontrado"
                ));
    }

    private Usuario obtenerUsuarioAutenticado(String correoUsuario) {
        if (correoUsuario == null || correoUsuario.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario autenticado no encontrado"
            );
        }

        return usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"
                ));
    }

    private void validarUsuarioAdmision(Usuario usuario) {
        validarRolEIpress(usuario);

        if (!ROL_ADMISION.equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permiso para consultar historial de archivos"
            );
        }
    }

    private void validarUsuarioConsultaProcesados(Usuario usuario) {
        validarRolEIpress(usuario);

        String nombreRol = usuario.getRol().getNombreRol();
        if (!Set.of(ROL_ADMISION, ROL_HOSPITALIZACION).stream()
                .anyMatch(rol -> rol.equalsIgnoreCase(nombreRol))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permiso para consultar archivos procesados"
            );
        }
    }

    private void validarRolEIpress(Usuario usuario) {
        if (usuario.getRol() == null
                || usuario.getRol().getNombreRol() == null
                || usuario.getRol().getNombreRol().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene un rol asignado"
            );
        }

        if (usuario.getIpress() == null || usuario.getIpress().getIdIpress() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene una IPRESS asignada"
            );
        }
    }

    private ArchivoProcesadoDTO convertirAProcesadoDTO(ArchivoCargado archivo) {
        List<RegistroHospitalario> registros =
                registroHospitalarioRepository.findByArchivoCargado_IdArchivo(
                        archivo.getIdArchivo()
                );

        ArchivoProcesadoDTO dto = new ArchivoProcesadoDTO();
        dto.setIdArchivo(archivo.getIdArchivo());
        dto.setNombreArchivo(archivo.getNombreArchivo());
        dto.setFechaCarga(archivo.getFechaCarga());
        dto.setFormatoDetectado(archivo.getFormato());
        dto.setRegistrosValidos(registros.size());
        dto.setPrediccionesGeneradas(
                prediccionRiesgoRepository
                        .findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(
                                archivo.getIdArchivo()
                        )
                        .size()
        );

        registros.stream()
                .map(RegistroHospitalario::getAnio)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .ifPresent(dto::setAnioMinimo);
        registros.stream()
                .map(RegistroHospitalario::getAnio)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .ifPresent(dto::setAnioMaximo);

        return dto;
    }
}
