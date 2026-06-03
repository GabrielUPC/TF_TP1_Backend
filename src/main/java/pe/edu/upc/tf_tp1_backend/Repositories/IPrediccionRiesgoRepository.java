package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPrediccionRiesgoRepository extends JpaRepository<PrediccionRiesgo, Integer> {

    Optional<PrediccionRiesgo> findByIndicadorHospitalario_IdIndicador(Integer idIndicador);

    List<PrediccionRiesgo> findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(Long idArchivo);
}