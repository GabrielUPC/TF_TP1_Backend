package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

public class RegistroHospitalarioImportado {

    private int numeroFila;
    private String codigoIpress;
    private String nombreIpress;
    private String categoriaIpress;
    private String codigoUbigeo;
    private String departamento;
    private String provincia;
    private String distrito;
    private String sector;
    private String idHospitalizacion;
    private String servicioHospitalario;
    private Integer anio;
    private Integer mes;
    private Integer ingresos;
    private Integer egresos;
    private Integer estancias;
    private Integer pacientesCama;
    private Integer camasTotales;
    private Integer camasDisponiblesHabilitadas;
    private Integer totalCamasDisponibles;
    private Integer fallecidos;

    public RegistroHospitalarioImportado() {
    }

    public RegistroHospitalarioImportado(RegistroHospitalarioImportado otro) {
        this.numeroFila = otro.numeroFila;
        this.codigoIpress = otro.codigoIpress;
        this.nombreIpress = otro.nombreIpress;
        this.categoriaIpress = otro.categoriaIpress;
        this.codigoUbigeo = otro.codigoUbigeo;
        this.departamento = otro.departamento;
        this.provincia = otro.provincia;
        this.distrito = otro.distrito;
        this.sector = otro.sector;
        this.idHospitalizacion = otro.idHospitalizacion;
        this.servicioHospitalario = otro.servicioHospitalario;
        this.anio = otro.anio;
        this.mes = otro.mes;
        this.ingresos = otro.ingresos;
        this.egresos = otro.egresos;
        this.estancias = otro.estancias;
        this.pacientesCama = otro.pacientesCama;
        this.camasTotales = otro.camasTotales;
        this.camasDisponiblesHabilitadas = otro.camasDisponiblesHabilitadas;
        this.totalCamasDisponibles = otro.totalCamasDisponibles;
        this.fallecidos = otro.fallecidos;
    }

    public int getNumeroFila() {
        return numeroFila;
    }

    public void setNumeroFila(int numeroFila) {
        this.numeroFila = numeroFila;
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

    public String getCategoriaIpress() {
        return categoriaIpress;
    }

    public void setCategoriaIpress(String categoriaIpress) {
        this.categoriaIpress = categoriaIpress;
    }

    public String getCodigoUbigeo() {
        return codigoUbigeo;
    }

    public void setCodigoUbigeo(String codigoUbigeo) {
        this.codigoUbigeo = codigoUbigeo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIdHospitalizacion() {
        return idHospitalizacion;
    }

    public void setIdHospitalizacion(String idHospitalizacion) {
        this.idHospitalizacion = idHospitalizacion;
    }

    public String getServicioHospitalario() {
        return servicioHospitalario;
    }

    public void setServicioHospitalario(String servicioHospitalario) {
        this.servicioHospitalario = servicioHospitalario;
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

    public Integer getTotalCamasDisponibles() {
        return totalCamasDisponibles;
    }

    public void setTotalCamasDisponibles(Integer totalCamasDisponibles) {
        this.totalCamasDisponibles = totalCamasDisponibles;
    }

    public Integer getFallecidos() {
        return fallecidos;
    }

    public void setFallecidos(Integer fallecidos) {
        this.fallecidos = fallecidos;
    }
}
