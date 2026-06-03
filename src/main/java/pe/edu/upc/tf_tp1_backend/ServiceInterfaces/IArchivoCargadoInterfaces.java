package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import java.util.List;

public interface IArchivoCargadoInterfaces {

    public List<ArchivoCargado> list();

    public void add(ArchivoCargado archivoCargado);

    public ArchivoCargado listId(Long id);

    public void modificar(ArchivoCargado archivoCargado);

    public void eliminar(Long id);
}