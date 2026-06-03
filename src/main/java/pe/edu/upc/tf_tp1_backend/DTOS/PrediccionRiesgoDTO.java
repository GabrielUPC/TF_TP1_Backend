package pe.edu.upc.tf_tp1_backend.DTOS;

import java.time.LocalDateTime;

public class PrediccionRiesgoDTO {

    private Integer idPrediccion;
    private Integer idIndicador;
    private String nivelRiesgo;
    private Double probabilidad;
    private String modeloUtilizado;
    private LocalDateTime fechaPrediccion;

    public Integer getIdPrediccion() {
        return idPrediccion;
    }

    public void setIdPrediccion(Integer idPrediccion) {
        this.idPrediccion = idPrediccion;
    }

    public Integer getIdIndicador() {
        return idIndicador;
    }

    public void setIdIndicador(Integer idIndicador) {
        this.idIndicador = idIndicador;
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