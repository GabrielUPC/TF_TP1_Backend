package pe.edu.upc.tf_tp1_backend.DTOS;

import java.util.List;

public class ResumenCargaExcelDTO {

    private Long idArchivo;
    private String nombreArchivo;
    private String formato;
    private String estadoValidacion;
    private String estadoProcesamiento;

    private Integer totalFilasLeidas;
    private Integer registrosValidos;
    private Integer registrosConErrores;

    private String mensaje;
    private List<ErrorValidacionDTO> errores;

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

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }

    public String getEstadoProcesamiento() {
        return estadoProcesamiento;
    }

    public void setEstadoProcesamiento(String estadoProcesamiento) {
        this.estadoProcesamiento = estadoProcesamiento;
    }

    public Integer getTotalFilasLeidas() {
        return totalFilasLeidas;
    }

    public void setTotalFilasLeidas(Integer totalFilasLeidas) {
        this.totalFilasLeidas = totalFilasLeidas;
    }

    public Integer getRegistrosValidos() {
        return registrosValidos;
    }

    public void setRegistrosValidos(Integer registrosValidos) {
        this.registrosValidos = registrosValidos;
    }

    public Integer getRegistrosConErrores() {
        return registrosConErrores;
    }

    public void setRegistrosConErrores(Integer registrosConErrores) {
        this.registrosConErrores = registrosConErrores;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<ErrorValidacionDTO> getErrores() {
        return errores;
    }

    public void setErrores(List<ErrorValidacionDTO> errores) {
        this.errores = errores;
    }
}