package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.tf_tp1_backend.CargaHospitalaria.HallazgoCalidadImportado;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionRiesgoListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.PrediccionRiesgo;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IPrediccionRiesgoRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrediccionRiesgoServiceImplementsTest {

    @Mock
    private IPrediccionRiesgoRepository prediccionRepository;

    @InjectMocks
    private PrediccionRiesgoServiceImplements service;

    @Test
    void exponeArchivoIpressYPeriodoPredichoConCambioDeAnio() {
        Ipress ipress = crearIpress();
        RegistroHospitalario diciembre = crearRegistro(20, 2016, 12, ipress);
        diciembre.getArchivoCargado().setIdArchivo(5L);
        diciembre.getArchivoCargado().setNombreArchivo("ConsultaD1_2016.csv");

        IndicadorHospitalario indicador = new IndicadorHospitalario();
        indicador.setIdIndicador(30);
        indicador.setRegistroHospitalario(diciembre);

        PrediccionRiesgo prediccion = new PrediccionRiesgo();
        prediccion.setIdPrediccion(40);
        prediccion.setIndicadorHospitalario(indicador);
        prediccion.setNivelRiesgo("ALTO");
        prediccion.setProbabilidad(0.9);
        prediccion.setModeloUtilizado("modelo_ipress_binario_v1");

        when(prediccionRepository
                .findByIndicadorHospitalario_RegistroHospitalario_ArchivoCargado_IdArchivo(5L))
                .thenReturn(List.of(prediccion));

        PrediccionRiesgoListDTO dto = service.listByArchivo(5L).get(0);

        assertEquals(5L, dto.getIdArchivo());
        assertEquals("ConsultaD1_2016.csv", dto.getNombreArchivo());
        assertEquals("00006207", dto.getCodigoIpress());
        assertEquals(2016, dto.getAnio());
        assertEquals(12, dto.getMes());
        assertEquals(2017, dto.getAnioPredicho());
        assertEquals(1, dto.getMesPredicho());
    }

    @Test
    void invalidaPrediccionActualYLaQueApuntaAlMesPendienteSinBorrarlas() {
        Ipress ipress = crearIpress();
        RegistroHospitalario enero = crearRegistro(50, 2026, 1, ipress);
        RegistroHospitalario febrero = crearRegistro(51, 2026, 2, ipress);
        RegistroHospitalario julio = crearRegistro(52, 2026, 7, ipress);
        PrediccionRiesgo pEnero = prediccion(enero);
        PrediccionRiesgo pFebrero = prediccion(febrero);
        PrediccionRiesgo pJulio = prediccion(julio);
        when(prediccionRepository.findAll()).thenReturn(List.of(pEnero, pFebrero, pJulio));

        service.invalidarPorPendientes(List.of(new HallazgoCalidadImportado(
                7, "00006207", 2026, 2, "HOSPITALIZACION GENERAL", "Q05", "pendiente")));

        assertEquals(false, pEnero.getVigente());
        assertEquals(false, pFebrero.getVigente());
        assertEquals(false, pJulio.getVigente());
        verify(prediccionRepository).saveAll(List.of(pEnero, pFebrero, pJulio));
    }

    private PrediccionRiesgo prediccion(RegistroHospitalario registro) {
        IndicadorHospitalario indicador = new IndicadorHospitalario();
        indicador.setRegistroHospitalario(registro);
        PrediccionRiesgo prediccion = new PrediccionRiesgo();
        prediccion.setIndicadorHospitalario(indicador);
        prediccion.setModeloUtilizado("modelo_ipress_binario_v1");
        prediccion.setVigente(true);
        return prediccion;
    }

    private Ipress crearIpress() {
        Ipress ipress = new Ipress();
        ipress.setIdIpress(99L);
        ipress.setCodigoRenipress("00006207");
        return ipress;
    }

    private RegistroHospitalario crearRegistro(Integer id, Integer anio, Integer mes, Ipress ipress) {
        ArchivoCargado archivo = new ArchivoCargado();
        archivo.setIpress(ipress);
        RegistroHospitalario registro = new RegistroHospitalario();
        registro.setIdRegistro(id);
        registro.setArchivoCargado(archivo);
        registro.setAnio(anio);
        registro.setMes(mes);
        registro.setServicioHospitalario("HOSPITALIZACION GENERAL");
        return registro;
    }
}
