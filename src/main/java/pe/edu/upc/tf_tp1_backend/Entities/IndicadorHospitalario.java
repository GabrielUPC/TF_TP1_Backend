package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "indicador_hospitalario")
public class IndicadorHospitalario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_indicador")
    private Integer idIndicador;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro", nullable = false, unique = true)
    private RegistroHospitalario registroHospitalario;

    @Column(name = "ocupacion_estimada")
    private Double ocupacionEstimada;

    @Column(name = "presion_ingresos_camas")
    private Double presionIngresosCamas;

    @Column(name = "promedio_estancia")
    private Double promedioEstancia;

    @Column(name = "rotacion_camas")
    private Double rotacionCamas;

    public IndicadorHospitalario() {
    }

    public Integer getIdIndicador() {
        return idIndicador;
    }

    public void setIdIndicador(Integer idIndicador) {
        this.idIndicador = idIndicador;
    }

    public RegistroHospitalario getRegistroHospitalario() {
        return registroHospitalario;
    }

    public void setRegistroHospitalario(RegistroHospitalario registroHospitalario) {
        this.registroHospitalario = registroHospitalario;
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