package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.Entities.Ipress;

import java.util.List;

public interface IIpressInterfaces {
    public List<Ipress> list();

    public void add(Ipress ipress);

    public Ipress listId(Long id);

    public void modificar(Ipress ipress);

    public void eliminar(Long id);
}
