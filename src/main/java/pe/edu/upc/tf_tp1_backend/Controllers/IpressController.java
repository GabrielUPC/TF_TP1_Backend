package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.IpressDTO;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIpressInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IUsuarioInterfaces;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ipress")
@CrossOrigin(origins = "http://localhost:4200")
public class IpressController {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Autowired
    private IIpressInterfaces ipressService;

    @Autowired
    private IUsuarioInterfaces usuarioService;

    @GetMapping
    public List<IpressDTO> listar(Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        return ipressService.list().stream().map(ipress -> {
            IpressDTO dto = new IpressDTO();

            dto.setIdIpress(ipress.getIdIpress());
            dto.setCodigoRenipress(ipress.getCodigoRenipress());
            dto.setNombreIpress(ipress.getNombreIpress());
            dto.setCategoriaIpress(ipress.getCategoriaIpress());
            dto.setCodigoUbigeo(ipress.getCodigoUbigeo());
            dto.setDistrito(ipress.getDistrito());
            dto.setProvincia(ipress.getProvincia());
            dto.setDepartamento(ipress.getDepartamento());

            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public void registrar(@RequestBody IpressDTO dto, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Ipress ipress = new Ipress();

        ipress.setIdIpress(dto.getIdIpress());
        ipress.setCodigoRenipress(dto.getCodigoRenipress());
        ipress.setNombreIpress(dto.getNombreIpress());
        ipress.setCategoriaIpress(dto.getCategoriaIpress());
        ipress.setCodigoUbigeo(dto.getCodigoUbigeo());
        ipress.setDistrito(dto.getDistrito());
        ipress.setProvincia(dto.getProvincia());
        ipress.setDepartamento(dto.getDepartamento());

        ipressService.add(ipress);
    }

    @GetMapping("/{id}")
    public IpressDTO listarId(@PathVariable("id") Long id, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Ipress ipress = ipressService.listId(id);

        IpressDTO dto = new IpressDTO();

        dto.setIdIpress(ipress.getIdIpress());
        dto.setCodigoRenipress(ipress.getCodigoRenipress());
        dto.setNombreIpress(ipress.getNombreIpress());
        dto.setCategoriaIpress(ipress.getCategoriaIpress());
        dto.setCodigoUbigeo(ipress.getCodigoUbigeo());
        dto.setDistrito(ipress.getDistrito());
        dto.setProvincia(ipress.getProvincia());
        dto.setDepartamento(ipress.getDepartamento());

        return dto;
    }

    @PutMapping
    public void modificar(@RequestBody IpressDTO dto, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        Ipress ipress = new Ipress();

        ipress.setIdIpress(dto.getIdIpress());
        ipress.setCodigoRenipress(dto.getCodigoRenipress());
        ipress.setNombreIpress(dto.getNombreIpress());
        ipress.setCategoriaIpress(dto.getCategoriaIpress());
        ipress.setCodigoUbigeo(dto.getCodigoUbigeo());
        ipress.setDistrito(dto.getDistrito());
        ipress.setProvincia(dto.getProvincia());
        ipress.setDepartamento(dto.getDepartamento());

        ipressService.modificar(ipress);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Long id, Authentication authentication) {
        obtenerAdministradorAutenticado(authentication);

        ipressService.eliminar(id);
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
