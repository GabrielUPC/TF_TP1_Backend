package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;

public interface IRolRepository extends JpaRepository<Rol,Long> {
}
