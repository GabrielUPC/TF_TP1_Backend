package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardDetalleDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardResumenDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IDashboardInterfaces;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private IDashboardInterfaces dS;

    @GetMapping("/resumen")
    public DashboardResumenDTO obtenerResumenGeneral(
            @RequestParam(value = "idArchivo", required = false) Long idArchivo,
            Authentication authentication
    ) {
        return dS.obtenerResumenGeneral(authentication.getName(), idArchivo);
    }

    @GetMapping("/detalle")
    public List<DashboardDetalleDTO> obtenerDetalleGeneral(
            @RequestParam(value = "idArchivo", required = false) Long idArchivo,
            Authentication authentication
    ) {
        return dS.obtenerDetalleGeneral(authentication.getName(), idArchivo);
    }

    @GetMapping("/archivo/{idArchivo}")
    public List<DashboardDetalleDTO> obtenerDetallePorArchivo(
            @PathVariable("idArchivo") Long idArchivo,
            Authentication authentication
    ) {
        return dS.obtenerDetallePorArchivo(authentication.getName(), idArchivo);
    }

    @GetMapping("/riesgo/{nivelRiesgo}")
    public List<DashboardDetalleDTO> obtenerDetallePorRiesgo(
            @PathVariable("nivelRiesgo") String nivelRiesgo,
            Authentication authentication
    ) {
        return dS.obtenerDetallePorRiesgo(authentication.getName(), nivelRiesgo);
    }

    @GetMapping("/filtro")
    public List<DashboardDetalleDTO> filtrar(
            @RequestParam(value = "idArchivo", required = false) Long idArchivo,
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "servicioHospitalario", required = false) String servicioHospitalario,
            Authentication authentication
    ) {
        return dS.filtrar(
                authentication.getName(),
                idArchivo,
                anio,
                mes,
                servicioHospitalario
        );
    }

    @GetMapping("/alertas")
    public List<DashboardDetalleDTO> obtenerAlertas(
            @RequestParam(value = "idArchivo", required = false) Long idArchivo,
            Authentication authentication
    ) {
        return dS.obtenerAlertas(authentication.getName(), idArchivo);
    }
}
