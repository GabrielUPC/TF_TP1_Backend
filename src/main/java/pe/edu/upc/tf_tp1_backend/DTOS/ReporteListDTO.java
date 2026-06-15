package pe.edu.upc.tf_tp1_backend.DTOS;

import java.time.LocalDateTime;

public class ReporteListDTO {

    private Integer idReporte;
    private Integer idPrediccion;
    private Integer idIndicador;
    private Integer idRegistro;

    private Long idArchivo;
    private String nombreArchivo;

    private Integer anio;
    private Integer mes;
    private Integer anioPredicho;
    private Integer mesPredicho;
    private String servicioHospitalario;
    private String codigoIpress;

    private Double ocupacionEstimada;
    private Double presionIngresosCamas;
    private Double promedioEstancia;
    private Double rotacionCamas;

    private String nivelRiesgo;
    private Double probabilidad;
    private String modeloUtilizado;
    private LocalDateTime fechaPrediccion;

    private LocalDateTime fechaGeneracion;
    private String usuarioGenerador;
    private String rutaArchivo;

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

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

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public Long getIdArchivo() {
        return idArchivo;
    }

    public void setIdArchivo(Long idArchivo) {
        this.idArchivo = idArchivo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

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

    public Integer getAnioPredicho() {
        return anioPredicho;
    }

    public void setAnioPredicho(Integer anioPredicho) {
        this.anioPredicho = anioPredicho;
    }

    public Integer getMesPredicho() {
        return mesPredicho;
    }

    public void setMesPredicho(Integer mesPredicho) {
        this.mesPredicho = mesPredicho;
    }

    public String getServicioHospitalario() {
        return servicioHospitalario;
    }

    public void setServicioHospitalario(String servicioHospitalario) {
        this.servicioHospitalario = servicioHospitalario;
    }

    public String getCodigoIpress() {
        return codigoIpress;
    }

    public void setCodigoIpress(String codigoIpress) {
        this.codigoIpress = codigoIpress;
    }

    public Double getOcupacionEstimada() {
        return ocupacionEstimada;
    }

    public void setOcupacionEstimada(Double ocupacionEstimada) {
        this.ocupacionEstimada = ocupacionEstimada;
    }

    public Double getPresionIngresosCamas() {
        return presionIngresosCamas;
    }

    public void setPresionIngresosCamas(Double presionIngresosCamas) {
        this.presionIngresosCamas = presionIngresosCamas;
    }

    public Double getPromedioEstancia() {
        return promedioEstancia;
    }

    public void setPromedioEstancia(Double promedioEstancia) {
        this.promedioEstancia = promedioEstancia;
    }

    public Double getRotacionCamas() {
        return rotacionCamas;
    }

    public void setRotacionCamas(Double rotacionCamas) {
        this.rotacionCamas = rotacionCamas;
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

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getUsuarioGenerador() {
        return usuarioGenerador;
    }

    public void setUsuarioGenerador(String usuarioGenerador) {
        this.usuarioGenerador = usuarioGenerador;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }
}
