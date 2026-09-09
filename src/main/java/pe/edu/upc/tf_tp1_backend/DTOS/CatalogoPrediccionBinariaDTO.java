package pe.edu.upc.tf_tp1_backend.DTOS;

import java.util.List;

public class CatalogoPrediccionBinariaDTO {
    private String codigoIpress;
    private String nombreIpress;
    private List<ServicioPrediccionDTO> servicios = List.of();

    public String getCodigoIpress() { return codigoIpress; }
    public void setCodigoIpress(String v) { codigoIpress = v; }
    public String getNombreIpress() { return nombreIpress; }
    public void setNombreIpress(String v) { nombreIpress = v; }
    public List<ServicioPrediccionDTO> getServicios() { return servicios; }
    public void setServicios(List<ServicioPrediccionDTO> v) { servicios = v; }

    public static class ServicioPrediccionDTO {
        private String servicio;
        private List<String> periodosDisponibles = List.of();
        public ServicioPrediccionDTO() { }
        public ServicioPrediccionDTO(String servicio, List<String> periodos) {
            this.servicio = servicio;
            this.periodosDisponibles = periodos;
        }
        public String getServicio() { return servicio; }
        public void setServicio(String v) { servicio = v; }
        public List<String> getPeriodosDisponibles() { return periodosDisponibles; }
        public void setPeriodosDisponibles(List<String> v) { periodosDisponibles = v; }
    }
}
