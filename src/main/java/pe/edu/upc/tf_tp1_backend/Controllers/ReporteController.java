package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteListDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IReporteInterfaces;

import java.util.List;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private IReporteInterfaces rS;

    @PostMapping("/generar")
    public void generarReporte(@RequestBody ReporteDTO dto) {
        rS.generarReporte(dto);
    }

    @GetMapping
    public List<ReporteListDTO> listar() {
        return rS.list();
    }

    @GetMapping("/{idReporte}")
    public ReporteListDTO listarPorId(@PathVariable("idReporte") Integer idReporte) {
        return rS.listId(idReporte);
    }

    @GetMapping("/prediccion/{idPrediccion}")
    public ReporteListDTO listarPorPrediccion(@PathVariable("idPrediccion") Integer idPrediccion) {
        return rS.listByPrediccion(idPrediccion);
    }

    @GetMapping("/archivo/{idArchivo}")
    public List<ReporteListDTO> listarPorArchivo(@PathVariable("idArchivo") Long idArchivo) {
        return rS.listByArchivo(idArchivo);
    }

    @DeleteMapping("/{idReporte}")
    public void eliminar(@PathVariable("idReporte") Integer idReporte) {
        rS.delete(idReporte);
    }
}