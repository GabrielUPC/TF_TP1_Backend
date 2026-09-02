package pe.edu.upc.tf_tp1_backend.DTOS;

public class PendienteCalidadDTO {
    private Integer fila;
    private String codigoIpress;
    private Integer anio;
    private Integer mes;
    private String servicioHospitalario;
    private String regla;
    private String motivo;
    private String estado;
    public Integer getFila(){return fila;}
    public void setFila(Integer v){fila=v;}
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
    public String getMotivo(){return motivo;}
    public void setMotivo(String v){motivo=v;}
    public String getEstado(){return estado;}
    public void setEstado(String v){estado=v;}
}
