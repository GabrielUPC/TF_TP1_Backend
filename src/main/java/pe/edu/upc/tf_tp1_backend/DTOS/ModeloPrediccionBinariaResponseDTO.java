package pe.edu.upc.tf_tp1_backend.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.edu.upc.tf_tp1_backend.Enums.NivelRiesgoPresentacion;

public class ModeloPrediccionBinariaResponseDTO {

    @JsonProperty("probabilidad_alta_presion")
    private Double probabilidadAltaPresion;

    @JsonProperty("threshold")
    private Double threshold;

    @JsonProperty("alerta_alta_presion")
    private Boolean alertaAltaPresion;

    @JsonProperty("nivel_riesgo")
    private NivelRiesgoPresentacion nivelRiesgo;

    @JsonProperty("descripcion_riesgo")
    private String descripcionRiesgo;

    @JsonProperty("umbral_bajo_medio")
    private Double umbralBajoMedio;

    @JsonProperty("codigo_ipress")
    private String codigoIpress;

    @JsonProperty("servicio_hospitalizacion")
    private String servicioHospitalizacion;

    @JsonProperty("periodo_actual")
    private String periodoActual;

    @JsonProperty("periodo_predicho")
    private String periodoPredicho;

    @JsonProperty("horizonte_prediccion")
    private String horizontePrediccion;

    @JsonProperty("modelo")
    private String modelo;

    public Double getProbabilidadAltaPresion() { return probabilidadAltaPresion; }
    public void setProbabilidadAltaPresion(Double valor) { this.probabilidadAltaPresion = valor; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double valor) { this.threshold = valor; }
    public Boolean getAlertaAltaPresion() { return alertaAltaPresion; }
    public void setAlertaAltaPresion(Boolean valor) { this.alertaAltaPresion = valor; }
    public NivelRiesgoPresentacion getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(NivelRiesgoPresentacion valor) { this.nivelRiesgo = valor; }
    public String getDescripcionRiesgo() { return descripcionRiesgo; }
    public void setDescripcionRiesgo(String valor) { this.descripcionRiesgo = valor; }
    public Double getUmbralBajoMedio() { return umbralBajoMedio; }
    public void setUmbralBajoMedio(Double valor) { this.umbralBajoMedio = valor; }
    public String getCodigoIpress() { return codigoIpress; }
    public void setCodigoIpress(String valor) { this.codigoIpress = valor; }
    public String getServicioHospitalizacion() { return servicioHospitalizacion; }
    public void setServicioHospitalizacion(String valor) { this.servicioHospitalizacion = valor; }
    public String getPeriodoActual() { return periodoActual; }
    public void setPeriodoActual(String valor) { this.periodoActual = valor; }
    public String getPeriodoPredicho() { return periodoPredicho; }
    public void setPeriodoPredicho(String valor) { this.periodoPredicho = valor; }
    public String getHorizontePrediccion() { return horizontePrediccion; }
    public void setHorizontePrediccion(String valor) { this.horizontePrediccion = valor; }
    public String getModelo() { return modelo; }
    public void setModelo(String valor) { this.modelo = valor; }
}
