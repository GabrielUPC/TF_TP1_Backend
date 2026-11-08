package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.tf_tp1_backend.Entities.*;
import pe.edu.upc.tf_tp1_backend.Repositories.CalidadInferenciaRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cp036_calidad;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=test-only-secret-test-only-secret-test-only-secret-test-only-secret",
        "modelo.ipress.url=http://127.0.0.1:8000"
})
@Transactional
class CalidadInferenciaRepositoryTest {
    @Autowired private EntityManager em;
    @Autowired private CalidadInferenciaRepository calidad;

    @Test
    void pendienteHistoricoConTildeBloqueaServicioSinTildeSoloEnSuIpressYVentana() {
        Ipress origen = ipress("00001111");
        Ipress ajena = ipress("00002222");
        ArchivoCargado archivo = archivo(origen, "origen@qa.test");
        ArchivoCargado archivoAjeno = archivo(ajena, "ajeno@qa.test");
        HallazgoCalidadDatos q06 = new HallazgoCalidadDatos();
        q06.setArchivoCargado(archivo);
        q06.setAnio(2025); q06.setMes(3);
        q06.setServicioHospitalario("HOSPITALIZACIÓN DE MEDICINA");
        q06.setRegla("Q06"); q06.setEstado("PENDIENTE_VALIDACION");
        em.persist(q06);
        RegistroPendienteCalidadDatos pendiente = new RegistroPendienteCalidadDatos();
        pendiente.setArchivoCargado(archivoAjeno);
        pendiente.setAnio(2025); pendiente.setMes(3);
        pendiente.setServicioHospitalario("HOSPITALIZACION DE MEDICINA");
        pendiente.setEstado("PENDIENTE_VALIDACION");
        em.persist(pendiente);
        em.flush();

        assertTrue(calidad.existePendiente(origen.getIdIpress(),
                "hospitalizacion  de medicina", YearMonth.of(2025, 1), YearMonth.of(2025, 6)));
        assertFalse(calidad.existePendiente(origen.getIdIpress(),
                "HOSPITALIZACION DE PEDIATRIA", YearMonth.of(2025, 1), YearMonth.of(2025, 6)));
        assertFalse(calidad.existePendiente(origen.getIdIpress(),
                "HOSPITALIZACION DE MEDICINA", YearMonth.of(2025, 4), YearMonth.of(2025, 9)));
        assertTrue(calidad.existePendiente(ajena.getIdIpress(),
                "HOSPITALIZACION DE MEDICINA", YearMonth.of(2025, 1), YearMonth.of(2025, 6)));
    }

    private Ipress ipress(String codigo) {
        Ipress i = new Ipress();
        i.setCodigoRenipress(codigo); i.setNombreIpress("IPRESS " + codigo);
        em.persist(i);
        return i;
    }

    private ArchivoCargado archivo(Ipress ipress, String correo) {
        Rol rol = new Rol(); rol.setNombreRol("ROL_" + correo);
        em.persist(rol);
        Usuario usuario = new Usuario(); usuario.setNombre("QA"); usuario.setCorreo(correo);
        usuario.setContrasena("test-only"); usuario.setRol(rol); usuario.setIpress(ipress);
        em.persist(usuario);
        ArchivoCargado a = new ArchivoCargado(); a.setNombreArchivo("cp036.csv");
        a.setFormato("DATASET_D1"); a.setFechaCarga(LocalDateTime.of(2025, 3, 1, 0, 0));
        a.setEstadoValidacion("PENDIENTE_VALIDACION");
        a.setEstadoProcesamiento("SIN_PREDICCIONES"); a.setIpress(ipress); a.setUsuario(usuario);
        em.persist(a);
        return a;
    }
}
