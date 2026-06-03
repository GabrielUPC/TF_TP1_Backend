package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.RegistroHospitalarioDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.RegistroHospitalarioListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.RegistroHospitalario;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.Repositories.IRegistroHospitalarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IRegistroHospitalarioInterfaces;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistroHospitalarioServiceImplements implements IRegistroHospitalarioInterfaces {

    @Autowired
    private IRegistroHospitalarioRepository rR;

    @Autowired
    private IArchivoCargadoRepository aR;

    @Override
    public void insert(RegistroHospitalarioDTO dto) {

        ArchivoCargado archivo = aR.findById(dto.getIdArchivo())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Archivo cargado no encontrado"
                ));

        RegistroHospitalario registro = new RegistroHospitalario();

        if (dto.getIdRegistro() != null) {
            registro.setIdRegistro(dto.getIdRegistro());
        }

        registro.setArchivoCargado(archivo);
        registro.setAnio(dto.getAnio());
        registro.setMes(dto.getMes());
        registro.setServicioHospitalario(dto.getServicioHospitalario());
        registro.setIngresos(dto.getIngresos());
        registro.setEgresos(dto.getEgresos());
        registro.setEstancias(dto.getEstancias());
        registro.setPacientesCama(dto.getPacientesCama());
        registro.setCamasTotales(dto.getCamasTotales());
        registro.setCamasDisponiblesHabilitadas(dto.getCamasDisponiblesHabilitadas());

        rR.save(registro);
    }

    @Override
    public List<RegistroHospitalarioListDTO> list() {
        return rR.findAll().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RegistroHospitalarioListDTO listId(Integer idRegistro) {
        RegistroHospitalario registro = rR.findById(idRegistro)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registro hospitalario no encontrado"
                ));

        return convertToListDTO(registro);
    }

    @Override
    public List<RegistroHospitalarioListDTO> listByArchivo(Long idArchivo) {
        return rR.findByArchivoCargado_IdArchivo(idArchivo).stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer idRegistro) {
        if (!rR.existsById(idRegistro)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Registro hospitalario no encontrado"
            );
        }

        rR.deleteById(idRegistro);
    }

    private RegistroHospitalarioListDTO convertToListDTO(RegistroHospitalario registro) {

        RegistroHospitalarioListDTO dto = new RegistroHospitalarioListDTO();

        dto.setIdRegistro(registro.getIdRegistro());

        if (registro.getArchivoCargado() != null) {
            dto.setIdArchivo(registro.getArchivoCargado().getIdArchivo());
            dto.setNombreArchivo(registro.getArchivoCargado().getNombreArchivo());
        }

        dto.setAnio(registro.getAnio());
        dto.setMes(registro.getMes());
        dto.setServicioHospitalario(registro.getServicioHospitalario());
        dto.setIngresos(registro.getIngresos());
        dto.setEgresos(registro.getEgresos());
        dto.setEstancias(registro.getEstancias());
        dto.setPacientesCama(registro.getPacientesCama());
        dto.setCamasTotales(registro.getCamasTotales());
        dto.setCamasDisponiblesHabilitadas(registro.getCamasDisponiblesHabilitadas());

        return dto;
    }
}