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

    private String formatoDetectado;
    private Integer totalFilasInvalidas;
    private Integer totalRegistrosValidos;
    private Integer totalPrediccionesGeneradas;
    private Integer totalGruposPendientes;
    private Integer totalRegistrosPendientes;
    private List<PendienteCalidadDTO> pendientesCalidad;
    private List<String> advertencias;
    private List<String> columnasEncontradas;
    private List<String> columnasMinimasFormatoInterno;
    private List<String> columnasMinimasDatasetD1;

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

    public String getFormatoDetectado() {
        return formatoDetectado;
    }

    public void setFormatoDetectado(String formatoDetectado) {
        this.formatoDetectado = formatoDetectado;
    }

    public Integer getTotalFilasInvalidas() {
        return totalFilasInvalidas;
    }

    public void setTotalFilasInvalidas(Integer totalFilasInvalidas) {
        this.totalFilasInvalidas = totalFilasInvalidas;
    }

    public Integer getTotalRegistrosValidos() {
        return totalRegistrosValidos;
    }

    public void setTotalRegistrosValidos(Integer totalRegistrosValidos) {
        this.totalRegistrosValidos = totalRegistrosValidos;
    }

    public Integer getTotalPrediccionesGeneradas() {
        return totalPrediccionesGeneradas;
    }

    public void setTotalPrediccionesGeneradas(Integer totalPrediccionesGeneradas) {
        this.totalPrediccionesGeneradas = totalPrediccionesGeneradas;
    }
    public Integer getTotalGruposPendientes(){return totalGruposPendientes;}
    public void setTotalGruposPendientes(Integer v){totalGruposPendientes=v;}
    public Integer getTotalRegistrosPendientes(){return totalRegistrosPendientes;}
    public void setTotalRegistrosPendientes(Integer v){totalRegistrosPendientes=v;}
    public List<PendienteCalidadDTO> getPendientesCalidad(){return pendientesCalidad;}
    public void setPendientesCalidad(List<PendienteCalidadDTO> v){pendientesCalidad=v;}

    public List<String> getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(List<String> advertencias) {
        this.advertencias = advertencias;
    }

    public List<String> getColumnasEncontradas() {
        return columnasEncontradas;
    }

    public void setColumnasEncontradas(List<String> columnasEncontradas) {
        this.columnasEncontradas = columnasEncontradas;
    }

    public List<String> getColumnasMinimasFormatoInterno() {
        return columnasMinimasFormatoInterno;
    }

    public void setColumnasMinimasFormatoInterno(List<String> columnasMinimasFormatoInterno) {
        this.columnasMinimasFormatoInterno = columnasMinimasFormatoInterno;
    }

    public List<String> getColumnasMinimasDatasetD1() {
        return columnasMinimasDatasetD1;
    }

    public void setColumnasMinimasDatasetD1(List<String> columnasMinimasDatasetD1) {
        this.columnasMinimasDatasetD1 = columnasMinimasDatasetD1;
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
