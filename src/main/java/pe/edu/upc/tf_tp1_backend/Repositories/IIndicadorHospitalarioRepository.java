package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIndicadorHospitalarioRepository extends JpaRepository<IndicadorHospitalario, Integer> {

    Optional<IndicadorHospitalario> findByRegistroHospitalario_IdRegistro(Integer idRegistro);

    List<IndicadorHospitalario> findByRegistroHospitalario_ArchivoCargado_IdArchivo(Long idArchivo);

    @Query("""
            select h from IndicadorHospitalario h
            join fetch h.registroHospitalario r
            join fetch r.archivoCargado a
            join fetch a.ipress i
            where i.idIpress = :idIpress
            order by r.anio, r.mes, r.idRegistro
            """)
    List<IndicadorHospitalario> buscarPorIpress(
            @org.springframework.data.repository.query.Param("idIpress") Long idIpress
    );
}
