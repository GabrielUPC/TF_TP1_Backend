package pe.edu.upc.tf_tp1_backend.DTOS;

public class RegistroHospitalarioListDTO {

    private Integer idRegistro;
    private Long idArchivo;
    private String nombreArchivo;
    private Integer anio;
    private Integer mes;
    private String servicioHospitalario;
    private Integer ingresos;
    private Integer egresos;
    private Integer estancias;
    private Integer pacientesCama;
    private Integer camasTotales;
    private Integer camasDisponiblesHabilitadas;

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public Long getIdArchivo() {
        return idArchivo;
    }

    public void setIdArchivo(Long idArchivo) {
        this.idArchivo = idArchivo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public String getServicioHospitalario() {
        return servicioHospitalario;
    }

    public void setServicioHospitalario(String servicioHospitalario) {
        this.servicioHospitalario = servicioHospitalario;
    }

    public Integer getIngresos() {
        return ingresos;
    }

    public void setIngresos(Integer ingresos) {
        this.ingresos = ingresos;
    }

    public Integer getEgresos() {
        return egresos;
    }

    public void setEgresos(Integer egresos) {
        this.egresos = egresos;
    }

    public Integer getEstancias() {
        return estancias;
    }

    public void setEstancias(Integer estancias) {
        this.estancias = estancias;
    }

    public Integer getPacientesCama() {
        return pacientesCama;
    }

    public void setPacientesCama(Integer pacientesCama) {
        this.pacientesCama = pacientesCama;
    }

    public Integer getCamasTotales() {
        return camasTotales;
    }

    public void setCamasTotales(Integer camasTotales) {
        this.camasTotales = camasTotales;
    }

    public Integer getCamasDisponiblesHabilitadas() {
        return camasDisponiblesHabilitadas;
    }

    public void setCamasDisponiblesHabilitadas(Integer camasDisponiblesHabilitadas) {
        this.camasDisponiblesHabilitadas = camasDisponiblesHabilitadas;
    }
}