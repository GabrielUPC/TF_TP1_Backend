package pe.edu.upc.tf_tp1_backend.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ModeloPrediccionResponseDTO {

    @JsonProperty("periodo_actual")
    private String periodoActual;

    @JsonProperty("periodo_predicho")
    private String periodoPredicho;

    @JsonProperty("horizonte_prediccion")
    private String horizontePrediccion;

    @JsonProperty("nivel_riesgo_predicho")
    private String nivelRiesgoPredicho;

    @JsonProperty("nivel_riesgo_codificado")
    private Integer nivelRiesgoCodificado;

    @JsonProperty("probabilidad")
    private Double probabilidad;

    @JsonProperty("probabilidades_por_clase")
    private Map<String, Double> probabilidadesPorClase;

    @JsonProperty("variables_principales")
    private List<ModeloVariablePrincipalDTO> variablesPrincipales;

    @JsonProperty("advertencia_historial")
    private String advertenciaHistorial;

    @JsonProperty("mensaje")
    private String mensaje;

    public String getPeriodoActual() {
        return periodoActual;
    }

    public void setPeriodoActual(String periodoActual) {
        this.periodoActual = periodoActual;
    }

    public String getPeriodoPredicho() {
        return periodoPredicho;
    }

    public void setPeriodoPredicho(String periodoPredicho) {
        this.periodoPredicho = periodoPredicho;
    }

    public String getHorizontePrediccion() {
        return horizontePrediccion;
    }

    public void setHorizontePrediccion(String horizontePrediccion) {
        this.horizontePrediccion = horizontePrediccion;
    }

    public String getNivelRiesgoPredicho() {
        return nivelRiesgoPredicho;
    }

    public void setNivelRiesgoPredicho(String nivelRiesgoPredicho) {
        this.nivelRiesgoPredicho = nivelRiesgoPredicho;
    }

    public Integer getNivelRiesgoCodificado() {
        return nivelRiesgoCodificado;
    }

    public void setNivelRiesgoCodificado(Integer nivelRiesgoCodificado) {
        this.nivelRiesgoCodificado = nivelRiesgoCodificado;
    }

    public Double getProbabilidad() {
        return probabilidad;
    }

    public void setProbabilidad(Double probabilidad) {
        this.probabilidad = probabilidad;
    }

    public Map<String, Double> getProbabilidadesPorClase() {
        return probabilidadesPorClase;
    }

    public void setProbabilidadesPorClase(Map<String, Double> probabilidadesPorClase) {
        this.probabilidadesPorClase = probabilidadesPorClase;
    }

    public List<ModeloVariablePrincipalDTO> getVariablesPrincipales() {
        return variablesPrincipales;
    }

    public void setVariablesPrincipales(List<ModeloVariablePrincipalDTO> variablesPrincipales) {
        this.variablesPrincipales = variablesPrincipales;
    }

    public String getAdvertenciaHistorial() {
        return advertenciaHistorial;
    }

    public void setAdvertenciaHistorial(String advertenciaHistorial) {
        this.advertenciaHistorial = advertenciaHistorial;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
