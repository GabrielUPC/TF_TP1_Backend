package pe.edu.upc.tf_tp1_backend.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrediccionBinariaAutomaticaRequestDTO {
    @JsonProperty("servicio")
    private String servicio;

    @JsonProperty("periodo_actual")
    private String periodoActual;

    public String getServicio() { return servicio; }
    public void setServicio(String valor) { servicio = valor; }
    public String getPeriodoActual() { return periodoActual; }
    public void setPeriodoActual(String valor) { periodoActual = valor; }
}
