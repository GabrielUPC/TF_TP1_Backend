package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.IndicadorHospitalarioListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.IndicadorHospitalario;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IIndicadorHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIndicadorHospitalarioInterfaces;

import java.util.List;
import java.time.YearMonth;
import java.time.DateTimeException;
import java.util.stream.Collectors;

@Service
public class IndicadorHospitalarioServiceImplements implements IIndicadorHospitalarioInterfaces {

    @Autowired
    private IIndicadorHospitalarioRepository iR;

    @Autowired
    private IRegistroHospitalarioRepository rR;

    @Override
    @Transactional
    public void calcularPorRegistro(Integer idRegistro) {

        RegistroHospitalario registro = rR.findById(idRegistro)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro hospitalario no encontrado"
                ));

        calcularYGuardarIndicador(registro);
    }

    @Override
    @Transactional
    public void calcularPorArchivo(Long idArchivo) {

        List<RegistroHospitalario> registros = rR.findByArchivoCargado_IdArchivo(idArchivo);

        if (registros.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existen registros hospitalarios para el archivo indicado"
            );
        }

        for (RegistroHospitalario registro : registros) {
            calcularYGuardarIndicador(registro);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndicadorHospitalarioListDTO> list() {
        return iR.findAll().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IndicadorHospitalarioListDTO listId(Integer idIndicador) {

        IndicadorHospitalario indicador = iR.findById(idIndicador)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Indicador hospitalario no encontrado"
                ));

        return convertToListDTO(indicador);
    }

    @Override
    @Transactional(readOnly = true)
    public IndicadorHospitalarioListDTO listByRegistro(Integer idRegistro) {

        IndicadorHospitalario indicador = iR.findByRegistroHospitalario_IdRegistro(idRegistro)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe indicador calculado para este registro hospitalario"
                ));

        return convertToListDTO(indicador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndicadorHospitalarioListDTO> listByArchivo(Long idArchivo) {
        return iR.findByRegistroHospitalario_ArchivoCargado_IdArchivo(idArchivo).stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer idIndicador) {

        if (!iR.existsById(idIndicador)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Indicador hospitalario no encontrado"
            );
        }

        iR.deleteById(idIndicador);
    }

    private void calcularYGuardarIndicador(RegistroHospitalario registro) {

        IndicadorHospitalario indicador = iR.findByRegistroHospitalario_IdRegistro(registro.getIdRegistro())
                .orElse(new IndicadorHospitalario());

        indicador.setRegistroHospitalario(registro);

        // Camas físicas y días-cama mensuales tienen unidades diferentes.
        Number diasCamaDisponibles = obtenerDiasCamaDisponibles(registro);

        Double ocupacionEstimada = dividir(
                registro.getPacientesCama(),
                diasCamaDisponibles
        );

        Double presionIngresosCamas = dividir(
                registro.getIngresos(),
                registro.getCamasTotales()
        );

        Double promedioEstancia = dividir(
                registro.getEstancias(),
                registro.getEgresos()
        );

        Double rotacionCamas = dividir(
                registro.getEgresos(),
                registro.getCamasTotales()
        );

        indicador.setOcupacionEstimada(ocupacionEstimada);
        indicador.setPresionIngresosCamas(presionIngresosCamas);
        indicador.setPromedioEstancia(promedioEstancia);
        indicador.setRotacionCamas(rotacionCamas);

        iR.save(indicador);
    }

    private Number obtenerDiasCamaDisponibles(RegistroHospitalario registro) {
        if (registro.getTotalCamasDisponibles() != null) {
            return registro.getTotalCamasDisponibles(); // D1: ya son días-cama, incluido cero.
        }
        // Formato interno: misma conversión que el request a FastAPI; no sustituir por camas totales.
        if (registro.getCamasDisponiblesHabilitadas() == null
                || registro.getCamasDisponiblesHabilitadas() < 0
                || registro.getAnio() == null || registro.getMes() == null) return null;
        try {
            return registro.getCamasDisponiblesHabilitadas().doubleValue()
                    * YearMonth.of(registro.getAnio(), registro.getMes()).lengthOfMonth();
        } catch (DateTimeException error) {
            return null;
        }
    }

    private Double dividir(Integer numerador, Number denominador) {

        // Conserva el cero defensivo legado; no es una decisión predictiva.
        if (numerador == null || numerador < 0 || denominador == null
                || !Double.isFinite(denominador.doubleValue()) || denominador.doubleValue() <= 0) {
            return 0.0;
        }

        Double resultado = numerador.doubleValue() / denominador.doubleValue();

        return redondear(resultado);
    }

    private Double redondear(Double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private IndicadorHospitalarioListDTO convertToListDTO(IndicadorHospitalario indicador) {

        IndicadorHospitalarioListDTO dto = new IndicadorHospitalarioListDTO();

        dto.setIdIndicador(indicador.getIdIndicador());

        RegistroHospitalario registro = indicador.getRegistroHospitalario();

        if (registro != null) {
            dto.setIdRegistro(registro.getIdRegistro());
            dto.setAnio(registro.getAnio());
            dto.setMes(registro.getMes());
            dto.setServicioHospitalario(registro.getServicioHospitalario());
            dto.setIngresos(registro.getIngresos());
            dto.setEgresos(registro.getEgresos());
            dto.setEstancias(registro.getEstancias());
            dto.setPacientesCama(registro.getPacientesCama());
            dto.setCamasTotales(registro.getCamasTotales());
            dto.setCamasDisponiblesHabilitadas(registro.getCamasDisponiblesHabilitadas());

            ArchivoCargado archivo = registro.getArchivoCargado();

            if (archivo != null) {
                dto.setIdArchivo(archivo.getIdArchivo());
                dto.setNombreArchivo(archivo.getNombreArchivo());
            }
        }

        dto.setOcupacionEstimada(indicador.getOcupacionEstimada());
        dto.setPresionIngresosCamas(indicador.getPresionIngresosCamas());
        dto.setPromedioEstancia(indicador.getPromedioEstancia());
        dto.setRotacionCamas(indicador.getRotacionCamas());

        return dto;
    }
}
