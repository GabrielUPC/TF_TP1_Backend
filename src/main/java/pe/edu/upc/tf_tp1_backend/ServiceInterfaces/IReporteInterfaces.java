package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.ReporteDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteListDTO;

import java.util.List;

public interface IReporteInterfaces {

    void generarReporte(ReporteDTO dto);

    void generarPorArchivo(Long idArchivo, Long idUsuario);

    List<ReporteListDTO> list();

    ReporteListDTO listId(Integer idReporte);

    ReporteListDTO listByPrediccion(Integer idPrediccion);

    List<ReporteListDTO> listByArchivo(Long idArchivo);

    void delete(Integer idReporte);
}