package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hallazgo_calidad_datos")
public class HallazgoCalidadDatos {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHallazgo;
    @ManyToOne(optional = false) @JoinColumn(name = "id_archivo", nullable = false)
    private ArchivoCargado archivoCargado;
    private Integer filaOrigen;
    private String codigoIpress;
    private Integer anio;
    private Integer mes;
    @Column(length = 500) private String servicioHospitalario;
    @Column(length = 10) private String regla;
    @Column(length = 1000) private String descripcion;
    @Column(length = 50) private String versionPolitica;
    private String estado;
    private LocalDateTime fechaDeteccion;
    public Long getIdHallazgo(){return idHallazgo;}
    public ArchivoCargado getArchivoCargado(){return archivoCargado;}
    public void setArchivoCargado(ArchivoCargado v){archivoCargado=v;}
    public Integer getFilaOrigen(){return filaOrigen;}
    public void setFilaOrigen(Integer v){filaOrigen=v;}
    public String getCodigoIpress(){return codigoIpress;}
    public void setCodigoIpress(String v){codigoIpress=v;}
    public Integer getAnio(){return anio;}
    public void setAnio(Integer v){anio=v;}
    public Integer getMes(){return mes;}
    public void setMes(Integer v){mes=v;}
    public String getServicioHospitalario(){return servicioHospitalario;}
    public void setServicioHospitalario(String v){servicioHospitalario=v;}
    public String getRegla(){return regla;}
    public void setRegla(String v){regla=v;}
    public String getDescripcion(){return descripcion;}
    public void setDescripcion(String v){descripcion=v;}
    public String getVersionPolitica(){return versionPolitica;}
    public void setVersionPolitica(String v){versionPolitica=v;}
    public String getEstado(){return estado;}
    public void setEstado(String v){estado=v;}
    public LocalDateTime getFechaDeteccion(){return fechaDeteccion;}
    public void setFechaDeteccion(LocalDateTime v){fechaDeteccion=v;}
}
