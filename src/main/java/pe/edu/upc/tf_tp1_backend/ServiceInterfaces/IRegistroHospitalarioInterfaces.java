package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.RegistroHospitalarioDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.RegistroHospitalarioListDTO;

import java.util.List;

public interface IRegistroHospitalarioInterfaces {

    void insert(RegistroHospitalarioDTO dto);

    List<RegistroHospitalarioListDTO> list();

    RegistroHospitalarioListDTO listId(Integer idRegistro);

    List<RegistroHospitalarioListDTO> listByArchivo(Long idArchivo);

    void delete(Integer idRegistro);
}