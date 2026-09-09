package pe.edu.upc.tf_tp1_backend.DTOS;

public class DashboardResumenDTO {
    private Long totalRegistros;
    private Long totalPredicciones;
    private Long totalRiesgoBajo;
    private Long totalRiesgoMedio;
    private Long totalRiesgoAlto;
    private Double promedioOcupacionEstimada;
    private Double promedioPresionIngresosCamas;
    private Double promedioProbabilidadAltaPresion;
    private Integer totalIngresos;
    private Integer totalEgresos;
    private Integer totalEstancias;
    private Integer totalPacientesCama;
    private Integer totalCamasDisponiblesHabilitadas;
    private Integer totalDiasCamaDisponibles;
    private String nivelRiesgoPredominante;
    private String mensajeResumen;
    public Long getTotalRegistros(){return totalRegistros;} public void setTotalRegistros(Long v){totalRegistros=v;}
    public Long getTotalPredicciones(){return totalPredicciones;} public void setTotalPredicciones(Long v){totalPredicciones=v;}
    public Long getTotalRiesgoBajo(){return totalRiesgoBajo;} public void setTotalRiesgoBajo(Long v){totalRiesgoBajo=v;}
    public Long getTotalRiesgoMedio(){return totalRiesgoMedio;} public void setTotalRiesgoMedio(Long v){totalRiesgoMedio=v;}
    public Long getTotalRiesgoAlto(){return totalRiesgoAlto;} public void setTotalRiesgoAlto(Long v){totalRiesgoAlto=v;}
    public Double getPromedioOcupacionEstimada(){return promedioOcupacionEstimada;} public void setPromedioOcupacionEstimada(Double v){promedioOcupacionEstimada=v;}
    public Double getPromedioPresionIngresosCamas(){return promedioPresionIngresosCamas;} public void setPromedioPresionIngresosCamas(Double v){promedioPresionIngresosCamas=v;}
    public Double getPromedioProbabilidadAltaPresion(){return promedioProbabilidadAltaPresion;} public void setPromedioProbabilidadAltaPresion(Double v){promedioProbabilidadAltaPresion=v;}
    public Integer getTotalIngresos(){return totalIngresos;} public void setTotalIngresos(Integer v){totalIngresos=v;}
    public Integer getTotalEgresos(){return totalEgresos;} public void setTotalEgresos(Integer v){totalEgresos=v;}
    public Integer getTotalEstancias(){return totalEstancias;} public void setTotalEstancias(Integer v){totalEstancias=v;}
    public Integer getTotalPacientesCama(){return totalPacientesCama;} public void setTotalPacientesCama(Integer v){totalPacientesCama=v;}
    public Integer getTotalCamasDisponiblesHabilitadas(){return totalCamasDisponiblesHabilitadas;} public void setTotalCamasDisponiblesHabilitadas(Integer v){totalCamasDisponiblesHabilitadas=v;}
    public Integer getTotalDiasCamaDisponibles(){return totalDiasCamaDisponibles;} public void setTotalDiasCamaDisponibles(Integer v){totalDiasCamaDisponibles=v;}
    public String getNivelRiesgoPredominante(){return nivelRiesgoPredominante;} public void setNivelRiesgoPredominante(String v){nivelRiesgoPredominante=v;}
    public String getMensajeResumen(){return mensajeResumen;} public void setMensajeResumen(String v){mensajeResumen=v;}
}
