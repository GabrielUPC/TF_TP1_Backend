package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardDetalleDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardResumenDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IDashboardInterfaces;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private IDashboardInterfaces dS;

    @GetMapping("/resumen")
    public DashboardResumenDTO obtenerResumenGeneral() {
        return dS.obtenerResumenGeneral();
    }

    @GetMapping("/detalle")
    public List<DashboardDetalleDTO> obtenerDetalleGeneral() {
        return dS.obtenerDetalleGeneral();
    }

    @GetMapping("/archivo/{idArchivo}")
    public List<DashboardDetalleDTO> obtenerDetallePorArchivo(@PathVariable("idArchivo") Long idArchivo) {
        return dS.obtenerDetallePorArchivo(idArchivo);
    }

    @GetMapping("/riesgo/{nivelRiesgo}")
    public List<DashboardDetalleDTO> obtenerDetallePorRiesgo(@PathVariable("nivelRiesgo") String nivelRiesgo) {
        return dS.obtenerDetallePorRiesgo(nivelRiesgo);
    }

    @GetMapping("/filtro")
    public List<DashboardDetalleDTO> filtrar(
            @RequestParam(value = "anio", required = false) Integer anio,
            @RequestParam(value = "mes", required = false) Integer mes,
            @RequestParam(value = "servicioHospitalario", required = false) String servicioHospitalario
    ) {
        return dS.filtrar(anio, mes, servicioHospitalario);
    }

    @GetMapping("/alertas")
    public List<DashboardDetalleDTO> obtenerAlertas() {
        return dS.obtenerAlertas();
    }
}