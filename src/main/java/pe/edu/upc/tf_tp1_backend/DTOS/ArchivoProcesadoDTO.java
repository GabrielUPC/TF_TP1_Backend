package pe.edu.upc.tf_tp1_backend.DTOS;

import java.time.LocalDateTime;

public class ArchivoProcesadoDTO {

    private Long idArchivo;
    private String nombreArchivo;
    private LocalDateTime fechaCarga;
    private String formatoDetectado;
    private Integer anioMinimo;
    private Integer anioMaximo;
    private Integer registrosValidos;
    private Integer prediccionesGeneradas;

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

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public String getFormatoDetectado() {
        return formatoDetectado;
    }

    public void setFormatoDetectado(String formatoDetectado) {
        this.formatoDetectado = formatoDetectado;
    }

    public Integer getAnioMinimo() {
        return anioMinimo;
    }

    public void setAnioMinimo(Integer anioMinimo) {
        this.anioMinimo = anioMinimo;
    }

    public Integer getAnioMaximo() {
        return anioMaximo;
    }

    public void setAnioMaximo(Integer anioMaximo) {
        this.anioMaximo = anioMaximo;
    }

    public Integer getRegistrosValidos() {
        return registrosValidos;
    }

    public void setRegistrosValidos(Integer registrosValidos) {
        this.registrosValidos = registrosValidos;
    }

    public Integer getPrediccionesGeneradas() {
        return prediccionesGeneradas;
    }

    public void setPrediccionesGeneradas(Integer prediccionesGeneradas) {
        this.prediccionesGeneradas = prediccionesGeneradas;
    }
}
