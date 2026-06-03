package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.RegistroHospitalarioDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.RegistroHospitalarioListDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IRegistroHospitalarioInterfaces;

import java.util.List;

@RestController
@RequestMapping("/registros-hospitalarios")
public class RegistroHospitalarioController {

    @Autowired
    private IRegistroHospitalarioInterfaces rS;

    @PostMapping
    public void insertar(@RequestBody RegistroHospitalarioDTO dto) {
        rS.insert(dto);
    }

    @GetMapping
    public List<RegistroHospitalarioListDTO> listar() {
        return rS.list();
    }

    @GetMapping("/{idRegistro}")
    public RegistroHospitalarioListDTO listarPorId(@PathVariable("idRegistro") Integer idRegistro) {
        return rS.listId(idRegistro);
    }

    @GetMapping("/archivo/{idArchivo}")
    public List<RegistroHospitalarioListDTO> listarPorArchivo(@PathVariable("idArchivo") Long idArchivo) {
        return rS.listByArchivo(idArchivo);
    }

    @PutMapping
    public void modificar(@RequestBody RegistroHospitalarioDTO dto) {
        rS.insert(dto);
    }

    @DeleteMapping("/{idRegistro}")
    public void eliminar(@PathVariable("idRegistro") Integer idRegistro) {
        rS.delete(idRegistro);
    }
}