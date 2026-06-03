package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tf_tp1_backend.Entities.Reporte;

import java.util.List;
import java.util.Optional;

@Repository
public interface IReporteRepository extends JpaRepository<Reporte, Integer> {

    Optional<Reporte> findByPrediccionRiesgo_IdPrediccion(Integer idPrediccion);

    List<Reporte> findByPrediccionRiesgo_IndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(Long idArchivo);
}