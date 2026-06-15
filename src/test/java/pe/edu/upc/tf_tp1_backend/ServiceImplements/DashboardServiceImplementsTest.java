package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.tf_tp1_backend.DTOS.DashboardDetalleDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplementsTest {

    @Mock
    private IPrediccionRiesgoRepository prediccionRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    private DashboardServiceImplements service;

    @Test
    void filtraPorArchivoPeriodoServicioYSoloIpressAsignada() {
        Ipress ipressAsignada = ipress(1L, "00007636");
        Ipress otraIpress = ipress(2L, "00009999");
        Usuario usuario = usuarioHospitalizacion(ipressAsignada);

        PrediccionRiesgo esperada = prediccion(
                1,
                ipressAsignada,
                5L,
                2017,
                1,
                "UCI",
                "ALTO"
        );
        PrediccionRiesgo otroArchivo = prediccion(
                2,
                ipressAsignada,
                6L,
                2017,
                1,
                "UCI",
                "ALTO"
        );
        PrediccionRiesgo otraIpressPrediccion = prediccion(
                3,
                otraIpress,
                5L,
                2017,
                1,
                "UCI",
                "ALTO"
        );

        when(usuarioRepository.findByCorreo("hospital@correo.pe"))
                .thenReturn(Optional.of(usuario));
        when(prediccionRepository.findAll()).thenReturn(
                List.of(esperada, otroArchivo, otraIpressPrediccion)
        );

        List<DashboardDetalleDTO> resultado = service.filtrar(
                "hospital@correo.pe",
                5L,
                2017,
                1,
                "UCI"
        );

        assertEquals(1, resultado.size());
        DashboardDetalleDTO dto = resultado.get(0);
        assertEquals(1, dto.getIdPrediccion());
        assertEquals(5L, dto.getIdArchivo());
        assertEquals("00007636", dto.getCodigoIpress());
        assertEquals(2017, dto.getAnio());
        assertEquals(1, dto.getMes());
        assertEquals(2017, dto.getAnioPredicho());
        assertEquals(2, dto.getMesPredicho());
    }

    private Usuario usuarioHospitalizacion(Ipress ipress) {
        Rol rol = new Rol();
        rol.setNombreRol("ATENCION_HOSPITALIZACION");

        Usuario usuario = new Usuario();
        usuario.setCorreo("hospital@correo.pe");
        usuario.setRol(rol);
        usuario.setIpress(ipress);
        return usuario;
    }

    private PrediccionRiesgo prediccion(
            int id,
            Ipress ipress,
            long idArchivo,
            int anio,
            int mes,
            String servicio,
            String riesgo
    ) {
        ArchivoCargado archivo = new ArchivoCargado();
        archivo.setIdArchivo(idArchivo);
        archivo.setNombreArchivo("dataset-" + idArchivo + ".csv");
        archivo.setIpress(ipress);

        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setIdRegistro(id);
        registro.setArchivoCargado(archivo);
        registro.setAnio(anio);
        registro.setMes(mes);
        registro.setServicioHospitalario(servicio);

        IndicadorHospitalario indicador = new IndicadorHospitalario();
        indicador.setIdIndicador(id);
        indicador.setRegistroHospitalario(registro);

        PrediccionRiesgo prediccion = new PrediccionRiesgo();
        prediccion.setIdPrediccion(id);
        prediccion.setIndicadorHospitalario(indicador);
        prediccion.setNivelRiesgo(riesgo);
        prediccion.setProbabilidad(0.8);
        prediccion.setModeloUtilizado("XGBoost - FastAPI");
        return prediccion;
    }

    private Ipress ipress(Long id, String codigo) {
        Ipress ipress = new Ipress();
        ipress.setIdIpress(id);
        ipress.setCodigoRenipress(codigo);
        return ipress;
    }
}
