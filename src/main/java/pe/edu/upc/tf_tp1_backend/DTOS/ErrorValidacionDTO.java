package pe.edu.upc.tf_tp1_backend.DTOS;

public class ErrorValidacionDTO {

    private Integer fila;
    private String campo;
    private String tipoError;
    private String descripcion;
    private String recomendacion;

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getTipoError() {
        return tipoError;
    }

    public void setTipoError(String tipoError) {
        this.tipoError = tipoError;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRecomendacion() {
        return recomendacion;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }
}