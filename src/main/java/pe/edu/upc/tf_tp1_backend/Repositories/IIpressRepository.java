package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;

import java.util.Optional;

@Repository
public interface IIpressRepository extends JpaRepository<Ipress,Long> {
    Optional<Ipress> findByCodigoRenipressIgnoreCase(String codigoRenipress);
}
