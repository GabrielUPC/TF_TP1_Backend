package pe.edu.upc.tf_tp1_backend.DTOS;

public class IpressDTO {

    private Long idIpress;
    private String codigoRenipress;
    private String nombreIpress;
    private String categoriaIpress;
    private String codigoUbigeo;
    private String distrito;
    private String provincia;
    private String departamento;

    public Long getIdIpress() {
        return idIpress;
    }

    public void setIdIpress(Long idIpress) {
        this.idIpress = idIpress;
    }

    public String getCodigoRenipress() {
        return codigoRenipress;
    }

    public void setCodigoRenipress(String codigoRenipress) {
        this.codigoRenipress = codigoRenipress;
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