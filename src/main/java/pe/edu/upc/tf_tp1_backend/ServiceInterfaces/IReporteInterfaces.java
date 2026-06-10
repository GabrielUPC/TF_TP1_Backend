package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.ReporteDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteListDTO;

import java.util.List;

public interface IReporteInterfaces {

    void generarReporte(String correoUsuario, ReporteDTO dto);

    void generarPorArchivo(Long idArchivo, Long idUsuario);

    List<ReporteListDTO> list(String correoUsuario);

    ReporteListDTO listId(String correoUsuario, Integer idReporte);

    ReporteListDTO listByPrediccion(String correoUsuario, Integer idPrediccion);

    List<ReporteListDTO> listByArchivo(String correoUsuario, Long idArchivo);

    void delete(String correoUsuario, Integer idReporte);
}
