package pe.edu.upc.tf_tp1_backend.DTOS;

import java.time.LocalDateTime;

/** Registro observado con un bloque predictivo binario opcional. */
public class DashboardDetalleDTO {
    private Integer idIndicador;
    private Integer idRegistro;
    private Long idArchivo;
    private String nombreArchivo;
    private Integer anio;
    private Integer mes;
    private String servicioHospitalario;
    private String codigoIpress;
    private Integer ingresos;
    private Integer egresos;
    private Integer estancias;
    private Integer pacientesCama;
    private Integer camasTotales;
    private Integer camasDisponiblesHabilitadas;
    private Integer totalCamasDisponibles;
    private Double ocupacionEstimada;
    private Double presionIngresosCamas;
    private Double promedioEstancia;
    private Double rotacionCamas;

    private Integer idPrediccion;
    private Double probabilidadAltaPresion;
    private Double threshold;
    private Boolean alertaAltaPresion;
    private String nivelRiesgo;
    private String descripcionRiesgo;
    private String periodoPredicho;
    private String modeloUtilizado;
    private LocalDateTime fechaPrediccion;

    public Integer getIdIndicador(){return idIndicador;} public void setIdIndicador(Integer v){idIndicador=v;}
    public Integer getIdRegistro(){return idRegistro;} public void setIdRegistro(Integer v){idRegistro=v;}
    public Long getIdArchivo(){return idArchivo;} public void setIdArchivo(Long v){idArchivo=v;}
    public String getNombreArchivo(){return nombreArchivo;} public void setNombreArchivo(String v){nombreArchivo=v;}
    public Integer getAnio(){return anio;} public void setAnio(Integer v){anio=v;}
    public Integer getMes(){return mes;} public void setMes(Integer v){mes=v;}
    public String getServicioHospitalario(){return servicioHospitalario;} public void setServicioHospitalario(String v){servicioHospitalario=v;}
    public String getCodigoIpress(){return codigoIpress;} public void setCodigoIpress(String v){codigoIpress=v;}
    public Integer getIngresos(){return ingresos;} public void setIngresos(Integer v){ingresos=v;}
    public Integer getEgresos(){return egresos;} public void setEgresos(Integer v){egresos=v;}
    public Integer getEstancias(){return estancias;} public void setEstancias(Integer v){estancias=v;}
    public Integer getPacientesCama(){return pacientesCama;} public void setPacientesCama(Integer v){pacientesCama=v;}
    public Integer getCamasTotales(){return camasTotales;} public void setCamasTotales(Integer v){camasTotales=v;}
    public Integer getCamasDisponiblesHabilitadas(){return camasDisponiblesHabilitadas;} public void setCamasDisponiblesHabilitadas(Integer v){camasDisponiblesHabilitadas=v;}
    public Integer getTotalCamasDisponibles(){return totalCamasDisponibles;} public void setTotalCamasDisponibles(Integer v){totalCamasDisponibles=v;}
    public Double getOcupacionEstimada(){return ocupacionEstimada;} public void setOcupacionEstimada(Double v){ocupacionEstimada=v;}
    public Double getPresionIngresosCamas(){return presionIngresosCamas;} public void setPresionIngresosCamas(Double v){presionIngresosCamas=v;}
    public Double getPromedioEstancia(){return promedioEstancia;} public void setPromedioEstancia(Double v){promedioEstancia=v;}
    public Double getRotacionCamas(){return rotacionCamas;} public void setRotacionCamas(Double v){rotacionCamas=v;}
    public Integer getIdPrediccion(){return idPrediccion;} public void setIdPrediccion(Integer v){idPrediccion=v;}
    public Double getProbabilidadAltaPresion(){return probabilidadAltaPresion;} public void setProbabilidadAltaPresion(Double v){probabilidadAltaPresion=v;}
    public Double getThreshold(){return threshold;} public void setThreshold(Double v){threshold=v;}
    public Boolean getAlertaAltaPresion(){return alertaAltaPresion;} public void setAlertaAltaPresion(Boolean v){alertaAltaPresion=v;}
    public String getNivelRiesgo(){return nivelRiesgo;} public void setNivelRiesgo(String v){nivelRiesgo=v;}
    public String getDescripcionRiesgo(){return descripcionRiesgo;} public void setDescripcionRiesgo(String v){descripcionRiesgo=v;}
    public String getPeriodoPredicho(){return periodoPredicho;} public void setPeriodoPredicho(String v){periodoPredicho=v;}
    public String getModeloUtilizado(){return modeloUtilizado;} public void setModeloUtilizado(String v){modeloUtilizado=v;}
    public LocalDateTime getFechaPrediccion(){return fechaPrediccion;} public void setFechaPrediccion(LocalDateTime v){fechaPrediccion=v;}
}
