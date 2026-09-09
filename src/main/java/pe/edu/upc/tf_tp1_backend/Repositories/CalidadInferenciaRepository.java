package pe.edu.upc.tf_tp1_backend.Repositories;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;

@Repository
public class CalidadInferenciaRepository {
    private static final String ESTADO_PENDIENTE = "PENDIENTE_VALIDACION";
    private final EntityManager entityManager;

    public CalidadInferenciaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public boolean existePendiente(Long idIpress, String servicio, YearMonth desde, YearMonth hasta) {
        return contarPendientes("HallazgoCalidadDatos", idIpress, servicio, desde, hasta) > 0
                || contarPendientes("RegistroPendienteCalidadDatos", idIpress, servicio, desde, hasta) > 0;
    }

    private long contarPendientes(
            String entidad,
            Long idIpress,
            String servicio,
            YearMonth desde,
            YearMonth hasta
    ) {
        String jpql = "select count(e) from " + entidad + " e "
                + "join e.archivoCargado a join a.ipress i "
                + "where i.idIpress = :idIpress "
                + "and e.servicioHospitalario = :servicio "
                + "and (e.anio > :anioDesde or (e.anio = :anioDesde and e.mes >= :mesDesde)) "
                + "and (e.anio < :anioHasta or (e.anio = :anioHasta and e.mes <= :mesHasta)) "
                + "and e.estado = :estado";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("idIpress", idIpress)
                .setParameter("servicio", servicio)
                .setParameter("anioDesde", desde.getYear())
                .setParameter("mesDesde", desde.getMonthValue())
                .setParameter("anioHasta", hasta.getYear())
                .setParameter("mesHasta", hasta.getMonthValue())
                .setParameter("estado", ESTADO_PENDIENTE)
                .getSingleResult();
    }
}
