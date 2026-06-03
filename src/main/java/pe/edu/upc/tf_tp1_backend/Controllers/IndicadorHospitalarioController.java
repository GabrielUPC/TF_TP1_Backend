package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.IndicadorHospitalarioListDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIndicadorHospitalarioInterfaces;

import java.util.List;

@RestController
@RequestMapping("/indicadores-hospitalarios")
public class IndicadorHospitalarioController {

    @Autowired
    private IIndicadorHospitalarioInterfaces iS;

    @PostMapping("/calcular/registro/{idRegistro}")
    public void calcularPorRegistro(@PathVariable("idRegistro") Integer idRegistro) {
        iS.calcularPorRegistro(idRegistro);
    }

    @PostMapping("/calcular/archivo/{idArchivo}")
    public void calcularPorArchivo(@PathVariable("idArchivo") Long idArchivo) {
        iS.calcularPorArchivo(idArchivo);
    }

    @GetMapping
    public List<IndicadorHospitalarioListDTO> listar() {
        return iS.list();
    }

    @GetMapping("/{idIndicador}")
    public IndicadorHospitalarioListDTO listarPorId(@PathVariable("idIndicador") Integer idIndicador) {
        return iS.listId(idIndicador);
    }

    @GetMapping("/registro/{idRegistro}")
    public IndicadorHospitalarioListDTO listarPorRegistro(@PathVariable("idRegistro") Integer idRegistro) {
        return iS.listByRegistro(idRegistro);
    }

    @GetMapping("/archivo/{idArchivo}")
    public List<IndicadorHospitalarioListDTO> listarPorArchivo(@PathVariable("idArchivo") Long idArchivo) {
        return iS.listByArchivo(idArchivo);
    }

    @DeleteMapping("/{idIndicador}")
    public void eliminar(@PathVariable("idIndicador") Integer idIndicador) {
        iS.delete(idIndicador);
    }
}