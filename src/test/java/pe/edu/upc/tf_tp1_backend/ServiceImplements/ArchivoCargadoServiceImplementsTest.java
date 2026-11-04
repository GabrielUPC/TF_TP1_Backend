package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.tf_tp1_backend.DTOS.ArchivoProcesadoDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchivoCargadoServiceImplementsTest {

    @Test
    void historialConsultaSoloLaIpressAutenticadaSinAlterarFechaHistorica() {
        Ipress ipress = new Ipress();
        ipress.setIdIpress(9L);
        Rol rol = new Rol();
        rol.setNombreRol("ADMISION_REGISTROS");
        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setIpress(ipress);
        ArchivoCargado historico = archivo(5L, "DATASET_D1", "PROCESADO");
        historico.setIdArchivo(38L);
        historico.setFechaCarga(LocalDateTime.of(2026, 11, 1, 19, 37, 7));

        when(usuarioRepository.findByCorreo("admision@correo.pe"))
                .thenReturn(Optional.of(usuario));
        when(archivoRepository.findByIpress_IdIpress(9L))
                .thenReturn(List.of(historico));

        List<ArchivoCargado> resultado = service.listarPorUsuarioAutenticado("admision@correo.pe");

        assertEquals(List.of(historico), resultado);
        assertEquals(LocalDateTime.of(2026, 11, 1, 19, 37, 7), resultado.get(0).getFechaCarga());
        org.junit.jupiter.api.Assertions.assertNull(resultado.get(0).getFechaCargaUtc());
        org.mockito.Mockito.verify(archivoRepository).findByIpress_IdIpress(9L);
    }

    @Mock
    private IArchivoCargadoRepository archivoRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IRegistroHospitalarioRepository registroRepository;

    @Mock
    private IPrediccionRiesgoRepository prediccionRepository;

    @InjectMocks
    private ArchivoCargadoServiceImplements service;

    @Test
    void listaSoloProcesadosDeLaIpressConMetadatosAgregados() {
        Ipress ipress = new Ipress();
        ipress.setIdIpress(1L);

        Rol rol = new Rol();
        rol.setNombreRol("ATENCION_HOSPITALIZACION");

        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setIpress(ipress);

        ArchivoCargado procesado = archivo(5L, "DATASET_D1", "PROCESADO");
        ArchivoCargado error = archivo(6L, "DATASET_D1", "ERROR");

        RegistroHospitalario registro2016 = registro(2016);
        RegistroHospitalario registro2017 = registro(2017);

        when(usuarioRepository.findByCorreo("hospital@correo.pe"))
                .thenReturn(Optional.of(usuario));
        when(archivoRepository.findByIpress_IdIpress(1L))
                .thenReturn(List.of(error, procesado));
        when(registroRepository.findByArchivoCargado_IdArchivo(5L))
                .thenReturn(List.of(registro2016, registro2017));
        when(prediccionRepository
                .findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(
                        5L
                ))
                .thenReturn(List.of(prediccion("modelo_ipress_binario_v1"), prediccion("XGBoost - FastAPI")));

        List<ArchivoProcesadoDTO> resultado =
                service.listarProcesadosPorUsuarioAutenticado(
                        "hospital@correo.pe"
                );

        assertEquals(1, resultado.size());
        ArchivoProcesadoDTO dto = resultado.get(0);
        assertEquals(5L, dto.getIdArchivo());
        assertEquals("DATASET_D1", dto.getFormatoDetectado());
        assertEquals(2016, dto.getAnioMinimo());
        assertEquals(2017, dto.getAnioMaximo());
        assertEquals(2, dto.getRegistrosValidos());
        assertEquals(1, dto.getPrediccionesGeneradas());
    }

    private PrediccionRiesgo prediccion(String modelo) {
        PrediccionRiesgo p = new PrediccionRiesgo();
        p.setVigente(true);
        p.setModeloUtilizado(modelo);
        return p;
    }

    private ArchivoCargado archivo(
            Long id,
            String formato,
            String estadoProcesamiento
    ) {
        ArchivoCargado archivo = new ArchivoCargado();
        archivo.setIdArchivo(id);
        archivo.setNombreArchivo("dataset-" + id + ".csv");
        archivo.setFormato(formato);
        archivo.setEstadoProcesamiento(estadoProcesamiento);
        archivo.setFechaCarga(LocalDateTime.of(2026, 1, id.intValue(), 10, 0));
        return archivo;
    }

    private RegistroHospitalario registro(int anio) {
        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setAnio(anio);
        return registro;
    }
}
