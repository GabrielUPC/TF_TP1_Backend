package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "archivo_cargado")
public class ArchivoCargado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo")
    private Long idArchivo;

    @Column(name = "nombre_archivo", nullable = false, length = 200)
    private String nombreArchivo;

    @Column(name = "formato", nullable = false, length = 20)
    private String formato;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @Column(name = "estado_validacion", nullable = false, length = 50)
    private String estadoValidacion;

    @Column(name = "estado_procesamiento", nullable = false, length = 50)
    private String estadoProcesamiento;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_ipress", nullable = false)
    private Ipress ipress;

    public ArchivoCargado() {
    }

    public ArchivoCargado(Long idArchivo, String nombreArchivo, String formato, LocalDateTime fechaCarga,
                          String estadoValidacion, String estadoProcesamiento, Usuario usuario, Ipress ipress) {
        this.idArchivo = idArchivo;
        this.nombreArchivo = nombreArchivo;
        this.formato = formato;
        this.fechaCarga = fechaCarga;
        this.estadoValidacion = estadoValidacion;
        this.estadoProcesamiento = estadoProcesamiento;
        this.usuario = usuario;
        this.ipress = ipress;
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

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Ipress getIpress() {
        return ipress;
    }

    public void setIpress(Ipress ipress) {
        this.ipress = ipress;
    }
}