package pe.edu.upc.tf_tp1_backend.DTOS;

public class DashboardResumenDTO {

    private Long totalPredicciones;
    private Long totalRiesgoBajo;
    private Long totalRiesgoMedio;
    private Long totalRiesgoAlto;

    private Double promedioOcupacionEstimada;
    private Double promedioPresionIngresosCamas;
    private Double promedioProbabilidad;

    private Integer totalIngresos;
    private Integer totalEgresos;
    private Integer totalEstancias;
    private Integer totalPacientesCama;
    private Integer totalCamasDisponiblesHabilitadas;

    private String nivelRiesgoPredominante;
    private String mensajeResumen;

    public Long getTotalPredicciones() {
        return totalPredicciones;
    }

    public void setTotalPredicciones(Long totalPredicciones) {
        this.totalPredicciones = totalPredicciones;
    }

    public Long getTotalRiesgoBajo() {
        return totalRiesgoBajo;
    }

    public void setTotalRiesgoBajo(Long totalRiesgoBajo) {
        this.totalRiesgoBajo = totalRiesgoBajo;
    }

    public Long getTotalRiesgoMedio() {
        return totalRiesgoMedio;
    }

    public void setTotalRiesgoMedio(Long totalRiesgoMedio) {
        this.totalRiesgoMedio = totalRiesgoMedio;
    }

    public Long getTotalRiesgoAlto() {
        return totalRiesgoAlto;
    }

    public void setTotalRiesgoAlto(Long totalRiesgoAlto) {
        this.totalRiesgoAlto = totalRiesgoAlto;
    }

    public Double getPromedioOcupacionEstimada() {
        return promedioOcupacionEstimada;
    }

    public void setPromedioOcupacionEstimada(Double promedioOcupacionEstimada) {
        this.promedioOcupacionEstimada = promedioOcupacionEstimada;
    }

    public Double getPromedioPresionIngresosCamas() {
        return promedioPresionIngresosCamas;
    }

    public void setPromedioPresionIngresosCamas(Double promedioPresionIngresosCamas) {
        this.promedioPresionIngresosCamas = promedioPresionIngresosCamas;
    }

    public Double getPromedioProbabilidad() {
        return promedioProbabilidad;
    }

    public void setPromedioProbabilidad(Double promedioProbabilidad) {
        this.promedioProbabilidad = promedioProbabilidad;
    }

    public Integer getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(Integer totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public Integer getTotalEgresos() {
        return totalEgresos;
    }

    public void setTotalEgresos(Integer totalEgresos) {
        this.totalEgresos = totalEgresos;
    }

    public Integer getTotalEstancias() {
        return totalEstancias;
    }

    public void setTotalEstancias(Integer totalEstancias) {
        this.totalEstancias = totalEstancias;
    }

    public Integer getTotalPacientesCama() {
        return totalPacientesCama;
    }

    public void setTotalPacientesCama(Integer totalPacientesCama) {
        this.totalPacientesCama = totalPacientesCama;
    }

    public Integer getTotalCamasDisponiblesHabilitadas() {
        return totalCamasDisponiblesHabilitadas;
    }

    public void setTotalCamasDisponiblesHabilitadas(Integer totalCamasDisponiblesHabilitadas) {
        this.totalCamasDisponiblesHabilitadas = totalCamasDisponiblesHabilitadas;
    }

    public String getNivelRiesgoPredominante() {
        return nivelRiesgoPredominante;
    }

    public void setNivelRiesgoPredominante(String nivelRiesgoPredominante) {
        this.nivelRiesgoPredominante = nivelRiesgoPredominante;
    }

    public String getMensajeResumen() {
        return mensajeResumen;
    }

    public void setMensajeResumen(String mensajeResumen) {
        this.mensajeResumen = mensajeResumen;
    }
}