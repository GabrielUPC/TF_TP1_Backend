package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.RolDTO;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IRolInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IUsuarioInterfaces;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "http://localhost:4200")
public class RolController {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Autowired
    private IRolInterfaces rolService;

    @Autowired
    private IUsuarioInterfaces usuarioService;

    @GetMapping
    public List<RolDTO> listar(Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        return rolService.list().stream().map(rol -> {
            RolDTO dto = new RolDTO();
            dto.setIdRol(rol.getIdRol());
            dto.setNombreRol(rol.getNombreRol());
            dto.setDescripcion(rol.getDescripcion());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public void registrar(@RequestBody RolDTO dto, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Rol rol = new Rol();
        rol.setIdRol(dto.getIdRol());
        rol.setNombreRol(dto.getNombreRol());
        rol.setDescripcion(dto.getDescripcion());

        rolService.add(rol);
    }

    @GetMapping("/{id}")
    public RolDTO listarId(@PathVariable("id") Long id, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Rol rol = rolService.listId(id);

        RolDTO dto = new RolDTO();
        dto.setIdRol(rol.getIdRol());
        dto.setNombreRol(rol.getNombreRol());
        dto.setDescripcion(rol.getDescripcion());

        return dto;
    }

    @PutMapping
    public void modificar(@RequestBody RolDTO dto, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Rol rol = new Rol();
        rol.setIdRol(dto.getIdRol());
        rol.setNombreRol(dto.getNombreRol());
        rol.setDescripcion(dto.getDescripcion());

        rolService.modificar(rol);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Long id, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        rolService.eliminar(id);
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
