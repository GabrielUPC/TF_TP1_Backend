package pe.edu.upc.tf_tp1_backend.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "registro_hospitalario")
public class RegistroHospitalario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Integer idRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_archivo", nullable = false)
    private ArchivoCargado archivoCargado;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "servicio_hospitalario", nullable = false, length = 100)
    private String servicioHospitalario;

    @Column(name = "ingresos")
    private Integer ingresos;

    @Column(name = "egresos")
    private Integer egresos;

    @Column(name = "estancias")
    private Integer estancias;

    @Column(name = "pacientes_cama")
    private Integer pacientesCama;

    @Column(name = "camas_totales")
    private Integer camasTotales;

    @Column(name = "camas_disponibles_habilitadas")
    private Integer camasDisponiblesHabilitadas;

    public RegistroHospitalario() {
    }

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public ArchivoCargado getArchivoCargado() {
        return archivoCargado;
    }

    public void setArchivoCargado(ArchivoCargado archivoCargado) {
        this.archivoCargado = archivoCargado;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public String getServicioHospitalario() {
        return servicioHospitalario;
    }

    public void setServicioHospitalario(String servicioHospitalario) {
        this.servicioHospitalario = servicioHospitalario;
    }

    public Integer getIngresos() {
        return ingresos;
    }

    public void setIngresos(Integer ingresos) {
        this.ingresos = ingresos;
    }

    public Integer getEgresos() {
        return egresos;
    }

    public void setEgresos(Integer egresos) {
        this.egresos = egresos;
    }

    public Integer getEstancias() {
        return estancias;
    }

    public void setEstancias(Integer estancias) {
        this.estancias = estancias;
    }

    public Integer getPacientesCama() {
        return pacientesCama;
    }

    public void setPacientesCama(Integer pacientesCama) {
        this.pacientesCama = pacientesCama;
    }

    public Integer getCamasTotales() {
        return camasTotales;
    }

    public void setCamasTotales(Integer camasTotales) {
        this.camasTotales = camasTotales;
    }

    public Integer getCamasDisponiblesHabilitadas() {
        return camasDisponiblesHabilitadas;
    }

    public void setCamasDisponiblesHabilitadas(Integer camasDisponiblesHabilitadas) {
        this.camasDisponiblesHabilitadas = camasDisponiblesHabilitadas;
    }
}