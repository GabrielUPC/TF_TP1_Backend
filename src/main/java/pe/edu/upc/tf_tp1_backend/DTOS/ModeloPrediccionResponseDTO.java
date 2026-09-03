package pe.edu.upc.tf_tp1_backend.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ModeloPrediccionResponseDTO {

    @JsonProperty("periodo_actual")
    private String periodoActual;

    @JsonProperty("periodo_predicho")
    private String periodoPredicho;

    @JsonProperty("riesgo_insuficiencia_capacidad")
    // Índice operativo/visual legado, NO probabilidad calibrada de insuficiencia.
    private Double riesgoInsuficienciaCapacidad;

    @JsonProperty("horizonte_prediccion")
    private String horizontePrediccion;

    @JsonProperty("nivel_riesgo_predicho")
    private String nivelRiesgoPredicho;

    @JsonProperty("nivel_riesgo_codificado")
    private Integer nivelRiesgoCodificado;

    @JsonProperty("probabilidad")
    // Probabilidad de la clase FINAL elegida por Python; no necesariamente max(p).
    private Double probabilidad;

    @JsonProperty("probabilidades_por_clase")
    // Salidas originales: conservar sin reconstrucción ni renormalización.
    private Map<String, Double> probabilidadesPorClase;

    @JsonProperty("variables_principales")
    private List<ModeloVariablePrincipalDTO> variablesPrincipales;

    @JsonProperty("causa_principal_riesgo")
    private String causaPrincipalRiesgo;

    @JsonProperty("brecha_operativa")
    private Integer brechaOperativa;

    @JsonProperty("nivel_brecha_operativa")
    private String nivelBrechaOperativa;

    @JsonProperty("diagnostico_operativo")
    private String diagnosticoOperativo;

    @JsonProperty("recomendaciones_operativas")
    private List<String> recomendacionesOperativas;

    @JsonProperty("interpretacion_modelo")
    private String interpretacionModelo;

    @JsonProperty("confianza_prediccion")
    private Double confianzaPrediccion;

    @JsonProperty("probabilidad_riesgo_bajo")
    private Double probabilidadRiesgoBajo;

    @JsonProperty("probabilidad_riesgo_medio")
    private Double probabilidadRiesgoMedio;

    @JsonProperty("probabilidad_riesgo_alto")
    private Double probabilidadRiesgoAlto;

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

    public String getCausaPrincipalRiesgo() {
        return causaPrincipalRiesgo;
    }

    public void setCausaPrincipalRiesgo(String causaPrincipalRiesgo) {
        this.causaPrincipalRiesgo = causaPrincipalRiesgo;
    }

    public Integer getBrechaOperativa() {
        return brechaOperativa;
    }

    public void setBrechaOperativa(Integer brechaOperativa) {
        this.brechaOperativa = brechaOperativa;
    }

    public String getNivelBrechaOperativa() {
        return nivelBrechaOperativa;
    }

    public void setNivelBrechaOperativa(String nivelBrechaOperativa) {
        this.nivelBrechaOperativa = nivelBrechaOperativa;
    }

    public String getDiagnosticoOperativo() {
        return diagnosticoOperativo;
    }

    public void setDiagnosticoOperativo(String diagnosticoOperativo) {
        this.diagnosticoOperativo = diagnosticoOperativo;
    }

    public List<String> getRecomendacionesOperativas() {
        return recomendacionesOperativas;
    }

    public void setRecomendacionesOperativas(List<String> recomendacionesOperativas) {
        this.recomendacionesOperativas = recomendacionesOperativas;
    }

    public String getInterpretacionModelo() {
        return interpretacionModelo;
    }

    public void setInterpretacionModelo(String interpretacionModelo) {
        this.interpretacionModelo = interpretacionModelo;
    }

    public Double getConfianzaPrediccion() {
        return confianzaPrediccion;
    }

    public void setConfianzaPrediccion(Double confianzaPrediccion) {
        this.confianzaPrediccion = confianzaPrediccion;
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

    public Double getProbabilidadRiesgoAlto() {
        return probabilidadRiesgoAlto;
    }

    public void setProbabilidadRiesgoAlto(Double probabilidadRiesgoAlto) {
        this.probabilidadRiesgoAlto = probabilidadRiesgoAlto;
    }
    public Double getRiesgoInsuficienciaCapacidad() {
        return riesgoInsuficienciaCapacidad;
    }

    public void setRiesgoInsuficienciaCapacidad(Double riesgoInsuficienciaCapacidad) {
        this.riesgoInsuficienciaCapacidad = riesgoInsuficienciaCapacidad;
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
