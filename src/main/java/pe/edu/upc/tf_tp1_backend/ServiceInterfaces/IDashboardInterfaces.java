package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.DashboardDetalleDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardResumenDTO;

import java.util.List;

public interface IDashboardInterfaces {

    DashboardResumenDTO obtenerResumenGeneral();

    List<DashboardDetalleDTO> obtenerDetalleGeneral();

    List<DashboardDetalleDTO> obtenerDetallePorArchivo(Long idArchivo);

    List<DashboardDetalleDTO> obtenerDetallePorRiesgo(String nivelRiesgo);

    List<DashboardDetalleDTO> filtrar(Integer anio, Integer mes, String servicioHospitalario);

    List<DashboardDetalleDTO> obtenerAlertas();
}