package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.ArchivoProcesadoDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import java.util.List;

public interface IArchivoCargadoInterfaces {

    List<ArchivoCargado> listarPorUsuarioAutenticado(String correoUsuario);

    List<ArchivoProcesadoDTO> listarProcesadosPorUsuarioAutenticado(
            String correoUsuario
    );

    ArchivoCargado listarIdPorUsuarioAutenticado(Long idArchivo, String correoUsuario);
}
