package pe.edu.upc.tf_tp1_backend.DTOS;

public class IndicadorHospitalarioDTO {

    private Integer idIndicador;
    private Integer idRegistro;
    private Double ocupacionEstimada;
    private Double presionIngresosCamas;
    private Double promedioEstancia;
    private Double rotacionCamas;

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
}