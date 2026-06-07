package pe.edu.upc.tf_tp1_backend.Securities;

import java.io.Serializable;

public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;

    private String jwttoken;
    private Long idUsuario;
    private String nombre;
    private String correo;
    private String rol;
    private Long idIpress;
    private String nombreIpress;

    public JwtResponse() {
    }

    public JwtResponse(String jwttoken, Long idUsuario, String nombre, String correo, String rol, Long idIpress, String nombreIpress) {
        this.jwttoken = jwttoken;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.idIpress = idIpress;
        this.nombreIpress = nombreIpress;
    }

    public String getJwttoken() {
        return jwttoken;
    }

    public void setJwttoken(String jwttoken) {
        this.jwttoken = jwttoken;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Long getIdIpress() {
        return idIpress;
    }

    public void setIdIpress(Long idIpress) {
        this.idIpress = idIpress;
    }

    public String getNombreIpress() {
        return nombreIpress;
    }

    public void setNombreIpress(String nombreIpress) {
        this.nombreIpress = nombreIpress;
    }
}