package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.RolDTO;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IRolInterfaces;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "http://localhost:4200")
public class RolController {

    @Autowired
    private IRolInterfaces rolService;

    @GetMapping
    public List<RolDTO> listar() {
        return rolService.list().stream().map(rol -> {
            RolDTO dto = new RolDTO();
            dto.setIdRol(rol.getIdRol());
            dto.setNombreRol(rol.getNombreRol());
            dto.setDescripcion(rol.getDescripcion());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public void registrar(@RequestBody RolDTO dto) {
        Rol rol = new Rol();
        rol.setIdRol(dto.getIdRol());
        rol.setNombreRol(dto.getNombreRol());
        rol.setDescripcion(dto.getDescripcion());

        rolService.add(rol);
    }

    @GetMapping("/{id}")
    public RolDTO listarId(@PathVariable("id") Long id) {
        Rol rol = rolService.listId(id);

        RolDTO dto = new RolDTO();
        dto.setIdRol(rol.getIdRol());
        dto.setNombreRol(rol.getNombreRol());
        dto.setDescripcion(rol.getDescripcion());

        return dto;
    }

    @PutMapping
    public void modificar(@RequestBody RolDTO dto) {
        Rol rol = new Rol();
        rol.setIdRol(dto.getIdRol());
        rol.setNombreRol(dto.getNombreRol());
        rol.setDescripcion(dto.getDescripcion());

        rolService.modificar(rol);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Long id) {
        rolService.eliminar(id);
    }
}