package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ipress")
public class Ipress implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ipress")
    private Long idIpress;

    @Column(name = "codigo_ipress", nullable = false, unique = true, length = 30)
    private String codigoIpress;

    @Column(name = "nombre_ipress", nullable = false, length = 150)
    private String nombreIpress;

    @Column(name = "distrito", length = 80)
    private String distrito;

    @Column(name = "provincia", length = 80)
    private String provincia;

    @Column(name = "departamento", length = 80)
    private String departamento;

    public Ipress() {
    }

    public Ipress(Long idIpress, String codigoIpress, String nombreIpress, String distrito, String provincia, String departamento) {
        this.idIpress = idIpress;
        this.codigoIpress = codigoIpress;
        this.nombreIpress = nombreIpress;
        this.distrito = distrito;
        this.provincia = provincia;
        this.departamento = departamento;
    }

    public Long getIdIpress() {
        return idIpress;
    }

    public void setIdIpress(Long idIpress) {
        this.idIpress = idIpress;
    }

    public String getCodigoIpress() {
        return codigoIpress;
    }

    public void setCodigoIpress(String codigoIpress) {
        this.codigoIpress = codigoIpress;
    }

    public String getNombreIpress() {
        return nombreIpress;
    }

    public void setNombreIpress(String nombreIpress) {
        this.nombreIpress = nombreIpress;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
}