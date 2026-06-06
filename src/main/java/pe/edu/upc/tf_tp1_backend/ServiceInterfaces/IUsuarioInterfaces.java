package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import java.util.List;
import java.util.Optional;
public interface IUsuarioInterfaces {
    public List<Usuario> list();

    public void add(Usuario usuario);

    public Usuario listId(Long id);

    public void modificar(Usuario usuario);

    public Optional<Usuario> buscarPorCorreo(String correo);
}
