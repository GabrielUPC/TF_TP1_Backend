package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ArchivoCargadoDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ArchivoCargadoListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IArchivoCargadoInterfaces;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/archivos")
@CrossOrigin(origins = "http://localhost:4200")
public class ArchivoCargadoController {

    @Autowired
    private IArchivoCargadoInterfaces archivoService;

    @GetMapping
    public List<ArchivoCargadoListDTO> listar(Authentication authentication) {
        return archivoService.listarPorUsuarioAutenticado(authentication.getName()).stream()
                .map(this::convertirAListDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public void registrar() {
        lanzarGestionManualNoPermitida();
    }

    @GetMapping("/{id}")
    public ArchivoCargadoDTO listarId(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        ArchivoCargado archivo = archivoService.listarIdPorUsuarioAutenticado(
                id,
                authentication.getName()
        );

        return convertirADTO(archivo);
    }

    @PutMapping
    public void modificar() {
        lanzarGestionManualNoPermitida();
    }

    @DeleteMapping("/{id}")
    public void eliminar() {
        lanzarGestionManualNoPermitida();
    }

    private ArchivoCargadoListDTO convertirAListDTO(ArchivoCargado archivo) {
        ArchivoCargadoListDTO dto = new ArchivoCargadoListDTO();

        dto.setIdArchivo(archivo.getIdArchivo());
        dto.setNombreArchivo(archivo.getNombreArchivo());
        dto.setFormato(archivo.getFormato());
        dto.setFechaCarga(archivo.getFechaCarga());
        dto.setEstadoValidacion(archivo.getEstadoValidacion());
        dto.setEstadoProcesamiento(archivo.getEstadoProcesamiento());

        if (archivo.getUsuario() != null) {
            dto.setNombreUsuario(archivo.getUsuario().getNombre());
        }

        if (archivo.getIpress() != null) {
            dto.setNombreIpress(archivo.getIpress().getNombreIpress());
        }

        return dto;
    }

    private ArchivoCargadoDTO convertirADTO(ArchivoCargado archivo) {
        ArchivoCargadoDTO dto = new ArchivoCargadoDTO();

        dto.setIdArchivo(archivo.getIdArchivo());
        dto.setNombreArchivo(archivo.getNombreArchivo());
        dto.setFormato(archivo.getFormato());
        dto.setFechaCarga(archivo.getFechaCarga());
        dto.setEstadoValidacion(archivo.getEstadoValidacion());
        dto.setEstadoProcesamiento(archivo.getEstadoProcesamiento());

        if (archivo.getUsuario() != null) {
            dto.setIdUsuario(archivo.getUsuario().getIdUsuario());
        }

        if (archivo.getIpress() != null) {
            dto.setIdIpress(archivo.getIpress().getIdIpress());
        }

        return dto;
    }

    private void lanzarGestionManualNoPermitida() {
        throw new ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "La gestion manual de archivos no esta permitida. Use el modulo de carga Excel."
        );
    }
}
