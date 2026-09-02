package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionRiesgoListDTO;

import java.util.List;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.HallazgoCalidadImportado;

public interface IPrediccionRiesgoInterfaces {
    void invalidarPorPendientes(List<HallazgoCalidadImportado> pendientes);

    void predecirPorIndicador(Integer idIndicador);

    void predecirPorArchivo(Long idArchivo);

    List<PrediccionRiesgoListDTO> list();

    PrediccionRiesgoListDTO listId(Integer idPrediccion);

    PrediccionRiesgoListDTO listByIndicador(Integer idIndicador);

    List<PrediccionRiesgoListDTO> listByArchivo(Long idArchivo);

    void delete(Integer idPrediccion);
}
