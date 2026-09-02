package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_pendiente_calidad_datos")
public class RegistroPendienteCalidadDatos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRegistroPendiente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_archivo", nullable = false)
    private ArchivoCargado archivoCargado;

    private Integer filaOrigen;
    private String codigoIpress;
    private Integer anio;
    private Integer mes;
    @Column(length = 500)
    private String servicioHospitalario;
    @Column(length = 100)
    private String reglas;
    @Column(length = 1000)
    private String motivo;
    @Column(length = 50)
    private String versionPolitica;
    private String estado;
    private LocalDateTime fechaDeteccion;
    @Column(columnDefinition = "TEXT")
    private String datosRegistroJson;

    public Long getIdRegistroPendiente() { return idRegistroPendiente; }
    public ArchivoCargado getArchivoCargado() { return archivoCargado; }
    public void setArchivoCargado(ArchivoCargado valor) { archivoCargado = valor; }
    public Integer getFilaOrigen() { return filaOrigen; }
    public void setFilaOrigen(Integer valor) { filaOrigen = valor; }
    public String getCodigoIpress() { return codigoIpress; }
    public void setCodigoIpress(String valor) { codigoIpress = valor; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer valor) { anio = valor; }
    public Integer getMes() { return mes; }
    public void setMes(Integer valor) { mes = valor; }
    public String getServicioHospitalario() { return servicioHospitalario; }
    public void setServicioHospitalario(String valor) { servicioHospitalario = valor; }
    public String getReglas() { return reglas; }
    public void setReglas(String valor) { reglas = valor; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String valor) { motivo = valor; }
    public String getVersionPolitica() { return versionPolitica; }
    public void setVersionPolitica(String valor) { versionPolitica = valor; }
    public String getEstado() { return estado; }
    public void setEstado(String valor) { estado = valor; }
    public LocalDateTime getFechaDeteccion() { return fechaDeteccion; }
    public void setFechaDeteccion(LocalDateTime valor) { fechaDeteccion = valor; }
    public String getDatosRegistroJson() { return datosRegistroJson; }
    public void setDatosRegistroJson(String valor) { datosRegistroJson = valor; }
}
