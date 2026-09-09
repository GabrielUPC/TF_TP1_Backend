package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionRiesgoListDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IPrediccionRiesgoInterfaces;

import java.util.List;

@RestController
@RequestMapping("/predicciones-riesgo")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class PrediccionRiesgoController {

    @Autowired
    private IPrediccionRiesgoInterfaces pS;

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
