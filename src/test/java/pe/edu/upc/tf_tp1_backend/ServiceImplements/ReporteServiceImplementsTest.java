package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ReporteListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Reporte;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IReporteRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplementsTest {

    @Mock
    private IReporteRepository reporteRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IPrediccionRiesgoRepository prediccionRepository;

    @InjectMocks
    private ReporteServiceImplements service;

    @Test
    void filtraReportePorArchivoPeriodoServicioRiesgoEIpress() {
        Ipress ipressAsignada = ipress(1L, "00007636");
        Ipress otraIpress = ipress(2L, "00009999");

        when(usuarioRepository.findByCorreo("hospital@correo.pe"))
                .thenReturn(Optional.of(usuarioHospitalizacion(ipressAsignada)));
        when(reporteRepository.findAll()).thenReturn(List.of(
                reporte(1, ipressAsignada, 5L, 2016, 12, "UCI", "ALTO"),
                reporte(2, ipressAsignada, 5L, 2016, 12, "MEDICINA", "ALTO"),
                reporte(3, otraIpress, 5L, 2016, 12, "UCI", "ALTO")
        ));

        List<ReporteListDTO> resultado = service.filtrar(
                "hospital@correo.pe",
                5L,
                2016,
                12,
                "UCI",
                "alto"
        );

        assertEquals(1, resultado.size());
        ReporteListDTO dto = resultado.get(0);
        assertEquals(1, dto.getIdReporte());
        assertEquals("00007636", dto.getCodigoIpress());
        assertEquals(2016, dto.getAnio());
        assertEquals(12, dto.getMes());
        assertEquals(2017, dto.getAnioPredicho());
        assertEquals(1, dto.getMesPredicho());
    }

    @Test
    void noGeneraReporteParaPrediccionLegacyONoVigente() {
        Ipress ipress = ipress(1L, "00007636");
        when(usuarioRepository.findByCorreo("hospital@correo.pe"))
                .thenReturn(Optional.of(usuarioHospitalizacion(ipress)));
        PrediccionRiesgo prediccion = reporte(
                1, ipress, 5L, 2016, 12, "UCI", "ALTO"
        ).getPrediccionRiesgo();
        prediccion.setModeloUtilizado("XGBoost - FastAPI");
        when(prediccionRepository.findById(1)).thenReturn(Optional.of(prediccion));
        ReporteDTO solicitud = new ReporteDTO();
        solicitud.setIdPrediccion(1);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.generarReporte("hospital@correo.pe", solicitud));

        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(reporteRepository);
    }

    @Test
    void generarReporteReutilizaElExistenteParaLaMismaPrediccion() {
        Ipress ipress = ipress(1L, "00007636");
        Usuario usuario = usuarioHospitalizacion(ipress);
        usuario.setNombre("Responsable Hospitalario");
        Reporte existente = reporte(1, ipress, 5L, 2025, 4, "PEDIATRIA", "MEDIO");
        PrediccionRiesgo prediccion = existente.getPrediccionRiesgo();
        when(usuarioRepository.findByCorreo("hospital@correo.pe")).thenReturn(Optional.of(usuario));
        when(prediccionRepository.findById(1)).thenReturn(Optional.of(prediccion));
        when(reporteRepository.findByPrediccionRiesgo_IdPrediccion(1)).thenReturn(Optional.of(existente));
        ReporteDTO solicitud = new ReporteDTO();
        solicitud.setIdPrediccion(1);

        service.generarReporte("hospital@correo.pe", solicitud);
        service.generarReporte("hospital@correo.pe", solicitud);

        verify(reporteRepository, times(2)).save(existente);
        assertEquals(1, existente.getIdReporte());
        assertEquals(prediccion, existente.getPrediccionRiesgo());
        assertEquals("Responsable Hospitalario", existente.getUsuarioGenerador());
    }

    private Reporte reporte(
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
        prediccion.setProbabilidad(0.9);
        prediccion.setModeloUtilizado("modelo_ipress_binario_v1");
        prediccion.setVigente(true);

        Reporte reporte = new Reporte();
        reporte.setIdReporte(id);
        reporte.setPrediccionRiesgo(prediccion);
        reporte.setFechaGeneracion(LocalDateTime.of(2026, 1, id, 10, 0));
        return reporte;
    }

    private Usuario usuarioHospitalizacion(Ipress ipress) {
        Rol rol = new Rol();
        rol.setNombreRol("ATENCION_HOSPITALIZACION");

        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setIpress(ipress);
        return usuario;
    }

    private Ipress ipress(Long id, String codigo) {
        Ipress ipress = new Ipress();
        ipress.setIdIpress(id);
        ipress.setCodigoRenipress(codigo);
        return ipress;
    }
}
