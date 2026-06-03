package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionRiesgoListDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;

import java.util.List;

@RestController
@RequestMapping("/predicciones-riesgo")
public class PrediccionRiesgoController {

    @Autowired
    private IPrediccionRiesgoInterfaces pS;

    @PostMapping("/predecir/indicador/{idIndicador}")
    public void predecirPorIndicador(@PathVariable("idIndicador") Integer idIndicador) {
        pS.predecirPorIndicador(idIndicador);
    }

    @PostMapping("/predecir/archivo/{idArchivo}")
    public void predecirPorArchivo(@PathVariable("idArchivo") Long idArchivo) {
        pS.predecirPorArchivo(idArchivo);
    }

    @GetMapping
    public List<PrediccionRiesgoListDTO> listar() {
        return pS.list();
    }

    @GetMapping("/{idPrediccion}")
    public PrediccionRiesgoListDTO listarPorId(@PathVariable("idPrediccion") Integer idPrediccion) {
        return pS.listId(idPrediccion);
    }

    @GetMapping("/indicador/{idIndicador}")
    public PrediccionRiesgoListDTO listarPorIndicador(@PathVariable("idIndicador") Integer idIndicador) {
        return pS.listByIndicador(idIndicador);
    }

    @GetMapping("/archivo/{idArchivo}")
    public List<PrediccionRiesgoListDTO> listarPorArchivo(@PathVariable("idArchivo") Long idArchivo) {
        return pS.listByArchivo(idArchivo);
    }

    @DeleteMapping("/{idPrediccion}")
    public void eliminar(@PathVariable("idPrediccion") Integer idPrediccion) {
        pS.delete(idPrediccion);
    }
}