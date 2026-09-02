package pe.edu.upc.tf_tp1_backend.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroPendienteCalidadDatos;

public interface IRegistroPendienteCalidadDatosRepository
        extends JpaRepository<RegistroPendienteCalidadDatos, Long> {
}
