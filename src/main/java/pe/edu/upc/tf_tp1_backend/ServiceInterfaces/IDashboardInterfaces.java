package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.DashboardDetalleDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardResumenDTO;

import java.util.List;

public interface IDashboardInterfaces {

    DashboardResumenDTO obtenerResumenGeneral(String correoUsuario, Long idArchivo);

    List<DashboardDetalleDTO> obtenerDetalleGeneral(String correoUsuario, Long idArchivo);

    List<DashboardDetalleDTO> obtenerDetallePorArchivo(String correoUsuario, Long idArchivo);

    List<DashboardDetalleDTO> obtenerDetallePorRiesgo(String correoUsuario, String nivelRiesgo);

    List<DashboardDetalleDTO> filtrar(
            String correoUsuario,
            Long idArchivo,
            Integer anio,
            Integer mes,
            String servicioHospitalario
    );

    List<DashboardDetalleDTO> obtenerAlertas(String correoUsuario, Long idArchivo);
}
