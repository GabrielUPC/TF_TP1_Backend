package pe.edu.upc.tf_tp1_backend.Controllers;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:usuario_correo_test;MODE=PostgreSQL",
        "spring.jpa.database=H2",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UsuarioCorreoUnicoTest {

    @Autowired private IUsuarioRepository usuarios;
    @Autowired private EntityManager entityManager;

    private Rol rol;
    private Ipress primeraIpress;
    private Ipress segundaIpress;

    @BeforeEach
    void preparar() {
        rol = new Rol(null, "ADMISION_REGISTROS", null);
        primeraIpress = new Ipress(null, "QA-1", "IPRESS QA 1", null, null, null, null, null);
        segundaIpress = new Ipress(null, "QA-2", "IPRESS QA 2", null, null, null, null, null);
        entityManager.persist(rol);
        entityManager.persist(primeraIpress);
        entityManager.persist(segundaIpress);
        entityManager.flush();
    }

    @Test
    void baseDeDatosRechazaCorreoIgualEnDistintasIpress() {
        usuarios.saveAndFlush(usuario("duplicado@ipress.test", primeraIpress));

        assertThrows(DataIntegrityViolationException.class,
                () -> usuarios.saveAndFlush(usuario("duplicado@ipress.test", segundaIpress)));
    }

    @Test
    void baseDeDatosAceptaCorreosNuevosEnDistintasIpress() {
        usuarios.saveAndFlush(usuario("primero@ipress.test", primeraIpress));
        usuarios.saveAndFlush(usuario("segundo@ipress.test", segundaIpress));

        assertEquals(2, usuarios.count());
    }

    private Usuario usuario(String correo, Ipress ipress) {
        return new Usuario(null, "Usuario QA", correo, "clave-ya-cifrada", true, rol, ipress);
    }
}
