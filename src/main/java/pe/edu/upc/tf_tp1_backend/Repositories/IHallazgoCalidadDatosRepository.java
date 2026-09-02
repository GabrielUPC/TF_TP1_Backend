package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.tf_tp1_backend.Entities.HallazgoCalidadDatos;

public interface IHallazgoCalidadDatosRepository extends JpaRepository<HallazgoCalidadDatos, Long> {
    boolean existsByArchivoCargado_Ipress_IdIpressAndAnioAndMesAndServicioHospitalarioIgnoreCaseAndEstado(
            Long idIpress, Integer anio, Integer mes, String servicioHospitalario, String estado
    );
}
