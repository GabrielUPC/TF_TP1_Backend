package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.UsuarioDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.UsuarioListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIpressInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IRolInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IUsuarioInterfaces;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Autowired
    private IUsuarioInterfaces usuarioService;

    @Autowired
    private IRolInterfaces rolService;

    @Autowired
    private IIpressInterfaces ipressService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UsuarioListDTO> listar(Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        return usuarioService.list().stream().map(usuario -> {
            UsuarioListDTO dto = new UsuarioListDTO();

            dto.setIdUsuario(usuario.getIdUsuario());
            dto.setNombre(usuario.getNombre());
            dto.setCorreo(usuario.getCorreo());
            dto.setEstado(usuario.getEstado());

            if (usuario.getRol() != null) {
                dto.setNombreRol(usuario.getRol().getNombreRol());
            }

            if (usuario.getIpress() != null) {
                dto.setNombreIpress(usuario.getIpress().getNombreIpress());
            } else {
                dto.setNombreIpress("Sin IPRESS asignada");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public void registrar(@RequestBody UsuarioDTO dto, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(dto.getIdUsuario());
        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());

        String contrasenaEncriptada = passwordEncoder.encode(dto.getContrasena());
        usuario.setContrasena(contrasenaEncriptada);

        usuario.setEstado(dto.getEstado() != null ? dto.getEstado() : true);

        Rol rol = rolService.listId(dto.getIdRol());
        usuario.setRol(rol);

        if (dto.getIdIpress() != null) {
            Ipress ipress = ipressService.listId(dto.getIdIpress());
            usuario.setIpress(ipress);
        } else {
            usuario.setIpress(null);
        }

        usuarioService.add(usuario);
    }

    @GetMapping("/{id}")
    public UsuarioDTO listarId(@PathVariable("id") Long id, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Usuario usuario = usuarioService.listId(id);

        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setCorreo(usuario.getCorreo());
        dto.setEstado(usuario.getEstado());

        if (usuario.getRol() != null) {
            dto.setIdRol(usuario.getRol().getIdRol());
        }

        if (usuario.getIpress() != null) {
            dto.setIdIpress(usuario.getIpress().getIdIpress());
        }

        return dto;
    }

    @PutMapping
    public void modificar(@RequestBody UsuarioDTO dto, Authentication authentication) {
        Usuario usuarioAutenticado = obtenerAdministradorAutenticado(authentication);

        if (usuarioAutenticado.getIdUsuario().equals(dto.getIdUsuario())
                && Boolean.FALSE.equals(dto.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes inactivar tu propio usuario"
            );
        }

        Usuario usuario = usuarioService.listId(dto.getIdUsuario());

        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        usuario.setEstado(dto.getEstado());

        if (dto.getContrasena() != null && !dto.getContrasena().isBlank()) {
            String contrasenaEncriptada = passwordEncoder.encode(dto.getContrasena());
            usuario.setContrasena(contrasenaEncriptada);
        }

        Rol rol = rolService.listId(dto.getIdRol());
        usuario.setRol(rol);

        if (dto.getIdIpress() != null) {
            Ipress ipress = ipressService.listId(dto.getIdIpress());
            usuario.setIpress(ipress);
        } else {
            usuario.setIpress(null);
        }

        usuarioService.modificar(usuario);
    }
    @PutMapping("/{id}/inactivar")
    public void inactivar(@PathVariable("id") Long id, Authentication authentication) {

        Usuario usuarioAutenticado = obtenerAdministradorAutenticado(authentication);

        if (usuarioAutenticado.getIdUsuario().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes inactivar tu propio usuario"
            );
        }

        Usuario usuario = usuarioService.listId(id);
        usuario.setEstado(false);
        usuarioService.modificar(usuario);
    }

    @PutMapping("/{id}/activar")
    public void activar(@PathVariable("id") Long id, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Usuario usuario = usuarioService.listId(id);
        usuario.setEstado(true);
        usuarioService.modificar(usuario);
    }

    private Usuario obtenerAdministradorAutenticado(Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario autenticado no encontrado"
            );
        }

        Usuario usuario = usuarioService.buscarPorCorreo(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"
                ));

        if (usuario.getRol() == null
                || usuario.getRol().getNombreRol() == null
                || usuario.getRol().getNombreRol().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene un rol asignado"
            );
        }

        if (!ROL_ADMINISTRADOR.equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permiso para acceder al modulo administrativo"
            );
        }

        return usuario;
    }
}
