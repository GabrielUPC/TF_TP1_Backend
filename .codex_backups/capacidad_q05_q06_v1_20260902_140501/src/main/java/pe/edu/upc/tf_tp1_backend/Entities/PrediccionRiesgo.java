package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prediccion_riesgo")
public class PrediccionRiesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prediccion")
    private Integer idPrediccion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_indicador", nullable = false, unique = true)
    private IndicadorHospitalario indicadorHospitalario;

    @Column(name = "nivel_riesgo", nullable = false, length = 20)
    private String nivelRiesgo;

    @Column(name = "probabilidad")
    private Double probabilidad;

    @Column(name = "riesgo_insuficiencia_capacidad")
    private Double riesgoInsuficienciaCapacidad;

    @Column(name = "probabilidad_riesgo_alto")
    private Double probabilidadRiesgoAlto;

    @Column(name = "probabilidad_riesgo_bajo")
    private Double probabilidadRiesgoBajo;

    @Column(name = "probabilidad_riesgo_medio")
    private Double probabilidadRiesgoMedio;

    @Column(name = "modelo_utilizado", length = 100)
    private String modeloUtilizado;

    @Column(name = "fecha_prediccion")
    private LocalDateTime fechaPrediccion;

    public PrediccionRiesgo() {
    }

    public Integer getIdPrediccion() {
        return idPrediccion;
    }

    public void setIdPrediccion(Integer idPrediccion) {
        this.idPrediccion = idPrediccion;
    }

    public IndicadorHospitalario getIndicadorHospitalario() {
        return indicadorHospitalario;
    }

    public void setIndicadorHospitalario(IndicadorHospitalario indicadorHospitalario) {
        this.indicadorHospitalario = indicadorHospitalario;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public Double getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(Double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public Double getProbabilidadRiesgoAlto() {
        return probabilidadRiesgoAlto;
    }

    public void setProbabilidadRiesgoAlto(Double probabilidadRiesgoAlto) {
        this.probabilidadRiesgoAlto = probabilidadRiesgoAlto;
    }

    public Double getProbabilidadRiesgoBajo() {
        return probabilidadRiesgoBajo;
    }

    public void setProbabilidadRiesgoBajo(Double probabilidadRiesgoBajo) {
        this.probabilidadRiesgoBajo = probabilidadRiesgoBajo;
    }

    public Double getProbabilidadRiesgoMedio() {
        return probabilidadRiesgoMedio;
    }

    public void setProbabilidadRiesgoMedio(Double probabilidadRiesgoMedio) {
        this.probabilidadRiesgoMedio = probabilidadRiesgoMedio;
    }
    public Double getRiesgoInsuficienciaCapacidad() {
        return riesgoInsuficienciaCapacidad;
    }

    public void setRiesgoInsuficienciaCapacidad(Double riesgoInsuficienciaCapacidad) {
        this.riesgoInsuficienciaCapacidad = riesgoInsuficienciaCapacidad;
    }
    public String getModeloUtilizado() {
        return modeloUtilizado;
    }

    public void setModeloUtilizado(String modeloUtilizado) {
        this.modeloUtilizado = modeloUtilizado;
    }

    public LocalDateTime getFechaPrediccion() {
        return fechaPrediccion;
    }

    public void setFechaPrediccion(LocalDateTime fechaPrediccion) {
        this.fechaPrediccion = fechaPrediccion;
    }
}
