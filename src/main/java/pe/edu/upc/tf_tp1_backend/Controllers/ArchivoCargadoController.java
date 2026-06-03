package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.ArchivoCargadoDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ArchivoCargadoListDTO;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IArchivoCargadoInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIpressInterfaces;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IUsuarioInterfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/archivos")
public class ArchivoCargadoController {

    @Autowired
    private IArchivoCargadoInterfaces archivoService;

    @Autowired
    private IUsuarioInterfaces usuarioService;

    @Autowired
    private IIpressInterfaces ipressService;

    @GetMapping
    public List<ArchivoCargadoListDTO> listar() {
        return archivoService.list().stream().map(archivo -> {
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
        }).collect(Collectors.toList());
    }

    @PostMapping
    public void registrar(@RequestBody ArchivoCargadoDTO dto) {
        ArchivoCargado archivo = new ArchivoCargado();

        archivo.setIdArchivo(dto.getIdArchivo());
        archivo.setNombreArchivo(dto.getNombreArchivo());
        archivo.setFormato(dto.getFormato());

        if (dto.getFechaCarga() != null) {
            archivo.setFechaCarga(dto.getFechaCarga());
        } else {
            archivo.setFechaCarga(LocalDateTime.now());
        }

        archivo.setEstadoValidacion(dto.getEstadoValidacion());
        archivo.setEstadoProcesamiento(dto.getEstadoProcesamiento());

        Usuario usuario = usuarioService.listId(dto.getIdUsuario());
        archivo.setUsuario(usuario);

        Ipress ipress = ipressService.listId(dto.getIdIpress());
        archivo.setIpress(ipress);

        archivoService.add(archivo);
    }

    @GetMapping("/{id}")
    public ArchivoCargadoDTO listarId(@PathVariable("id") Long id) {
        ArchivoCargado archivo = archivoService.listId(id);

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

    @PutMapping
    public void modificar(@RequestBody ArchivoCargadoDTO dto) {
        ArchivoCargado archivo = new ArchivoCargado();

        archivo.setIdArchivo(dto.getIdArchivo());
        archivo.setNombreArchivo(dto.getNombreArchivo());
        archivo.setFormato(dto.getFormato());
        archivo.setFechaCarga(dto.getFechaCarga());
        archivo.setEstadoValidacion(dto.getEstadoValidacion());
        archivo.setEstadoProcesamiento(dto.getEstadoProcesamiento());

        Usuario usuario = usuarioService.listId(dto.getIdUsuario());
        archivo.setUsuario(usuario);

        Ipress ipress = ipressService.listId(dto.getIdIpress());
        archivo.setIpress(ipress);

        archivoService.modificar(archivo);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Long id) {
        archivoService.eliminar(id);
    }
}