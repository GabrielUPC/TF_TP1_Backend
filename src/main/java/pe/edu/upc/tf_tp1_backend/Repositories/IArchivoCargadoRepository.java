package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;

import java.util.List;
import java.util.Optional;

@Repository
public interface IArchivoCargadoRepository extends JpaRepository<ArchivoCargado, Long> {

    List<ArchivoCargado> findByIpress_IdIpress(Long idIpress);

    Optional<ArchivoCargado> findByIdArchivoAndIpress_IdIpress(Long idArchivo, Long idIpress);
}
