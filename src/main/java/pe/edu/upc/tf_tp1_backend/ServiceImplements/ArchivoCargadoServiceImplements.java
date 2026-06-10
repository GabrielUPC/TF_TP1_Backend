package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IArchivoCargadoInterfaces;

import java.util.List;

@Service
public class ArchivoCargadoServiceImplements implements IArchivoCargadoInterfaces {

    private static final String ROL_ADMISION = "ADMISION_REGISTROS";

    @Autowired
    private IArchivoCargadoRepository archivoCargadoRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

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
        if (usuario.getRol() == null
                || usuario.getRol().getNombreRol() == null
                || usuario.getRol().getNombreRol().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene un rol asignado"
            );
        }

        if (!ROL_ADMISION.equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permiso para consultar historial de archivos"
            );
        }

        if (usuario.getIpress() == null || usuario.getIpress().getIdIpress() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene una IPRESS asignada"
            );
        }
    }
}
