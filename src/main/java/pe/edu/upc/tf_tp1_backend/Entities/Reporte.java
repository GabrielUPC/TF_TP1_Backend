package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reporte")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer idReporte;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prediccion", nullable = false, unique = true)
    private PrediccionRiesgo prediccionRiesgo;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "usuario_generador", length = 120)
    private String usuarioGenerador;

    @Column(name = "ruta_archivo", length = 250)
    private String rutaArchivo;

    public Reporte() {
    }

    public Integer getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Integer idReporte) {
        this.idReporte = idReporte;
    }

    public PrediccionRiesgo getPrediccionRiesgo() {
        return prediccionRiesgo;
    }

    public void setPrediccionRiesgo(PrediccionRiesgo prediccionRiesgo) {
        this.prediccionRiesgo = prediccionRiesgo;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getUsuarioGenerador() {
        return usuarioGenerador;
    }

    public void setUsuarioGenerador(String usuarioGenerador) {
        this.usuarioGenerador = usuarioGenerador;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }
}