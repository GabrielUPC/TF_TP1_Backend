package pe.edu.upc.tf_tp1_backend.DTOS;

public class UsuarioDTO {

    private Long idUsuario;
    private String nombre;
    private String correo;
    private String contrasena;
    private Boolean estado;
    private Long idRol;
    private Long idIpress;

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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contraseña) {
        this.contrasena = contraseña;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public Long getIdIpress() {
        return idIpress;
    }

    public void setIdIpress(Long idIpress) {
        this.idIpress = idIpress;
    }
}