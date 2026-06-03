package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIndicadorHospitalarioRepository extends JpaRepository<IndicadorHospitalario, Integer> {

    Optional<IndicadorHospitalario> findByRegistroHospitalario_IdRegistro(Integer idRegistro);

    List<IndicadorHospitalario> findByRegistroHospitalario_ArchivoCargado_IdArchivo(Long idArchivo);
}