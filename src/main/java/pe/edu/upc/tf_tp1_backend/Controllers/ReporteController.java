package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    public void generarReporte(@RequestBody ReporteDTO dto, Authentication authentication) {
        rS.generarReporte(authentication.getName(), dto);
    }

    @GetMapping
    public List<ReporteListDTO> listar(
            @RequestParam(value = "idArchivo", required = false) Long idArchivo,
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "servicioHospitalario", required = false) String servicioHospitalario,
            @RequestParam(value = "nivelRiesgo", required = false) String nivelRiesgo,
            Authentication authentication
    ) {
        return rS.filtrar(
                authentication.getName(),
                idArchivo,
                anio,
                mes,
                servicioHospitalario,
                nivelRiesgo
        );
    }

    @GetMapping("/{idReporte}")
    public ReporteListDTO listarPorId(
            @PathVariable("idReporte") Integer idReporte,
            Authentication authentication
    ) {
        return rS.listId(authentication.getName(), idReporte);
    }

    @GetMapping("/prediccion/{idPrediccion}")
    public ReporteListDTO listarPorPrediccion(
            @PathVariable("idPrediccion") Integer idPrediccion,
            Authentication authentication
    ) {
        return rS.listByPrediccion(authentication.getName(), idPrediccion);
    }

    @GetMapping("/archivo/{idArchivo}")
    public List<ReporteListDTO> listarPorArchivo(
            @PathVariable("idArchivo") Long idArchivo,
            Authentication authentication
    ) {
        return rS.listByArchivo(authentication.getName(), idArchivo);
    }

    @DeleteMapping("/{idReporte}")
    public void eliminar(
            @PathVariable("idReporte") Integer idReporte,
            Authentication authentication
    ) {
        rS.delete(authentication.getName(), idReporte);
    }
}
