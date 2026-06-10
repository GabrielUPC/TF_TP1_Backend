package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import java.util.List;

public interface IArchivoCargadoInterfaces {

    List<ArchivoCargado> listarPorUsuarioAutenticado(String correoUsuario);

    ArchivoCargado listarIdPorUsuarioAutenticado(Long idArchivo, String correoUsuario);
}
