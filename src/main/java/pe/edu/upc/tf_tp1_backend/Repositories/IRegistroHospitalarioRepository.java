package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;

import java.util.List;

@Repository
public interface IRegistroHospitalarioRepository extends JpaRepository<RegistroHospitalario, Integer> {

    List<RegistroHospitalario> findByArchivoCargado_IdArchivo(Long idArchivo);

    @Query("""
            select r from RegistroHospitalario r
            join fetch r.archivoCargado a
            join fetch a.ipress i
            where i.idIpress = :idIpress
            order by r.anio, r.mes, r.idRegistro
            """)
    List<RegistroHospitalario> buscarPorIpress(@Param("idIpress") Long idIpress);

    List<RegistroHospitalario> findByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(
            Long idIpress,
            String servicioHospitalario
    );

    boolean existsByArchivoCargado_Ipress_IdIpressAndServicioHospitalarioIgnoreCase(
            Long idIpress,
            String servicioHospitalario
    );

    @Query("""
            select r
            from RegistroHospitalario r
            join fetch r.archivoCargado a
            join fetch a.ipress i
            where i.idIpress = :idIpress
              and lower(trim(r.servicioHospitalario)) = lower(trim(:servicio))
              and (r.anio > :anioDesde or (r.anio = :anioDesde and r.mes >= :mesDesde))
              and (r.anio < :anioHasta or (r.anio = :anioHasta and r.mes <= :mesHasta))
            order by r.anio, r.mes, r.idRegistro
            """)
    List<RegistroHospitalario> buscarVentanaInferencia(
            @Param("idIpress") Long idIpress,
            @Param("servicio") String servicio,
            @Param("anioDesde") Integer anioDesde,
            @Param("mesDesde") Integer mesDesde,
            @Param("anioHasta") Integer anioHasta,
            @Param("mesHasta") Integer mesHasta
    );
}
