package pe.edu.upc.tf_tp1_backend.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModeloDatosHospitalariosDTO {

    @JsonProperty("anio")
    private Integer anio;

    @JsonProperty("mes")
    private Integer mes;

    @JsonProperty("ubigeo")
    private String ubigeo;

    @JsonProperty("departamento")
    private String departamento;

    @JsonProperty("provincia")
    private String provincia;

    @JsonProperty("distrito")
    private String distrito;

    @JsonProperty("sector")
    private String sector;

    @JsonProperty("categoria_ipress")
    private String categoriaIpress;

    @JsonProperty("codigo_ipress")
    private String codigoIpress;

    @JsonProperty("id_hospitalizacion")
    private String idHospitalizacion;

    @JsonProperty("servicio_hospitalizacion")
    private String servicioHospitalizacion;

    @JsonProperty("total_ingresos")
    private Double totalIngresos;

    @JsonProperty("total_egresos")
    private Double totalEgresos;

    @JsonProperty("total_estancias")
    private Double totalEstancias;

    @JsonProperty("total_pacientes_camas")
    private Double totalPacientesCamas;

    @JsonProperty("total_camas")
    private Double totalCamas;

    @JsonProperty("total_camas_disponibles")
    private Double totalCamasDisponibles;

    @JsonProperty("total_fallecidos")
    private Double totalFallecidos;

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public String getUbigeo() {
        return ubigeo;
    }

    public void setUbigeo(String ubigeo) {
        this.ubigeo = ubigeo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getCategoriaIpress() {
        return categoriaIpress;
    }

    public void setCategoriaIpress(String categoriaIpress) {
        this.categoriaIpress = categoriaIpress;
    }

    public String getCodigoIpress() {
        return codigoIpress;
    }

    public void setCodigoIpress(String codigoIpress) {
        this.codigoIpress = codigoIpress;
    }

    public String getIdHospitalizacion() {
        return idHospitalizacion;
    }

    public void setIdHospitalizacion(String idHospitalizacion) {
        this.idHospitalizacion = idHospitalizacion;
    }

    public String getServicioHospitalizacion() {
        return servicioHospitalizacion;
    }

    public void setServicioHospitalizacion(String servicioHospitalizacion) {
        this.servicioHospitalizacion = servicioHospitalizacion;
    }

    public Double getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(Double totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public Double getTotalEgresos() {
        return totalEgresos;
    }

    public void setTotalEgresos(Double totalEgresos) {
        this.totalEgresos = totalEgresos;
    }

    public Double getTotalEstancias() {
        return totalEstancias;
    }

    public void setTotalEstancias(Double totalEstancias) {
        this.totalEstancias = totalEstancias;
    }

    public Double getTotalPacientesCamas() {
        return totalPacientesCamas;
    }

    public void setTotalPacientesCamas(Double totalPacientesCamas) {
        this.totalPacientesCamas = totalPacientesCamas;
    }

    public Double getTotalCamas() {
        return totalCamas;
    }

    public void setTotalCamas(Double totalCamas) {
        this.totalCamas = totalCamas;
    }

    public Double getTotalCamasDisponibles() {
        return totalCamasDisponibles;
    }

    public void setTotalCamasDisponibles(Double totalCamasDisponibles) {
        this.totalCamasDisponibles = totalCamasDisponibles;
    }

    public Double getTotalFallecidos() {
        return totalFallecidos;
    }

    public void setTotalFallecidos(Double totalFallecidos) {
        this.totalFallecidos = totalFallecidos;
    }
}
