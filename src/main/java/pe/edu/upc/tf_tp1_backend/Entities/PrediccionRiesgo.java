package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Prediccion binaria de alta presion del mes t+1 asociada al indicador del mes t. */
@Entity
@Table(name = "prediccion_riesgo")
public class PrediccionRiesgo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prediccion")
    private Integer idPrediccion;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_indicador", nullable = false, unique = true)
    private IndicadorHospitalario indicadorHospitalario;
    @Column(name = "probabilidad_alta_presion", nullable = false)
    private Double probabilidadAltaPresion;
    @Column(name = "threshold", nullable = false)
    private Double threshold;
    @Column(name = "alerta_alta_presion", nullable = false)
    private Boolean alertaAltaPresion;
    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    private String nivelRiesgo;
    @Column(name = "descripcion_riesgo", nullable = false, length = 500)
    private String descripcionRiesgo;
    @Column(name = "periodo_predicho", nullable = false, length = 7)
    private String periodoPredicho;
    @Column(name = "modelo_utilizado", nullable = false, length = 100)
    private String modeloUtilizado;
    @Column(name = "fecha_prediccion", nullable = false)
    private LocalDateTime fechaPrediccion;
    @Column(name = "vigente", nullable = false)
    private Boolean vigente = true;
    @Column(name = "motivo_no_vigente", length = 1000)
    private String motivoNoVigente;
    @Transient private Double probabilidadRiesgoAltoLegacy;
    @Transient private Double probabilidadRiesgoBajoLegacy;
    @Transient private Double probabilidadRiesgoMedioLegacy;
    @Transient private Double riesgoInsuficienciaCapacidadLegacy;

    public Integer getIdPrediccion() { return idPrediccion; }
    public void setIdPrediccion(Integer v) { idPrediccion = v; }
    public IndicadorHospitalario getIndicadorHospitalario() { return indicadorHospitalario; }
    public void setIndicadorHospitalario(IndicadorHospitalario v) { indicadorHospitalario = v; }
    public Double getProbabilidadAltaPresion() { return probabilidadAltaPresion; }
    public void setProbabilidadAltaPresion(Double v) { probabilidadAltaPresion = v; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double v) { threshold = v; }
    public Boolean getAlertaAltaPresion() { return alertaAltaPresion; }
    public void setAlertaAltaPresion(Boolean v) { alertaAltaPresion = v; }
    public String getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(String v) { nivelRiesgo = v; }
    public String getDescripcionRiesgo() { return descripcionRiesgo; }
    public void setDescripcionRiesgo(String v) { descripcionRiesgo = v; }
    public String getPeriodoPredicho() { return periodoPredicho; }
    public void setPeriodoPredicho(String v) { periodoPredicho = v; }
    public String getModeloUtilizado() { return modeloUtilizado; }
    public void setModeloUtilizado(String v) { modeloUtilizado = v; }
    public LocalDateTime getFechaPrediccion() { return fechaPrediccion; }
    public void setFechaPrediccion(LocalDateTime v) { fechaPrediccion = v; }
    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean v) { vigente = v; }
    public String getMotivoNoVigente() { return motivoNoVigente; }
    public void setMotivoNoVigente(String v) { motivoNoVigente = v; }

    /** Alias transitorios: permiten retirar el flujo legacy sin crear sus columnas en una BD limpia. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Transient public Double getProbabilidad() { return probabilidadAltaPresion; }
    public void setProbabilidad(Double v) { probabilidadAltaPresion = v; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Transient public Double getProbabilidadRiesgoAlto() { return probabilidadRiesgoAltoLegacy; }
    public void setProbabilidadRiesgoAlto(Double v) { probabilidadRiesgoAltoLegacy = v; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Transient public Double getProbabilidadRiesgoBajo() { return probabilidadRiesgoBajoLegacy; }
    public void setProbabilidadRiesgoBajo(Double v) { probabilidadRiesgoBajoLegacy = v; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Transient public Double getProbabilidadRiesgoMedio() { return probabilidadRiesgoMedioLegacy; }
    public void setProbabilidadRiesgoMedio(Double v) { probabilidadRiesgoMedioLegacy = v; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Transient public Double getRiesgoInsuficienciaCapacidad() { return riesgoInsuficienciaCapacidadLegacy; }
    public void setRiesgoInsuficienciaCapacidad(Double v) { riesgoInsuficienciaCapacidadLegacy = v; }
}
