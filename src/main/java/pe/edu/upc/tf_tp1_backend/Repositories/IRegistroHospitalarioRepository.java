package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;

import java.util.List;

@Repository
public interface IRegistroHospitalarioRepository extends JpaRepository<RegistroHospitalario, Integer> {

    List<RegistroHospitalario> findByArchivoCargado_IdArchivo(Long idArchivo);

    List<RegistroHospitalario> findByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(
            Long idIpress,
            String servicioHospitalario
    );
}
