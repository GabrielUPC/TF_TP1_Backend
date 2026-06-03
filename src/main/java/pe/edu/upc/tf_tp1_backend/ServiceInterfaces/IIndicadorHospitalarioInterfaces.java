package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.IndicadorHospitalarioListDTO;

import java.util.List;

public interface IIndicadorHospitalarioInterfaces {

    void calcularPorRegistro(Integer idRegistro);

    void calcularPorArchivo(Long idArchivo);

    List<IndicadorHospitalarioListDTO> list();

    IndicadorHospitalarioListDTO listId(Integer idIndicador);

    IndicadorHospitalarioListDTO listByRegistro(Integer idRegistro);

    List<IndicadorHospitalarioListDTO> listByArchivo(Long idArchivo);

    void delete(Integer idIndicador);
}