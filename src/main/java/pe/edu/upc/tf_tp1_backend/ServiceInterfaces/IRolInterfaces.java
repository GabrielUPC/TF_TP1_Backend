package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.Entities.Rol;

import java.util.List;

public interface IRolInterfaces {
    public List<Rol> list();

    public void add(Rol rol);

    public Rol listId(Long id);

    public void modificar(Rol rol);

    public void eliminar(Long id);
}
