package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.tf_tp1_backend.DTOS.IpressDTO;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIpressInterfaces;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ipress")
public class IpressController {

    @Autowired
    private IIpressInterfaces ipressService;

    @GetMapping
    public List<IpressDTO> listar() {
        return ipressService.list().stream().map(ipress -> {
            IpressDTO dto = new IpressDTO();

            dto.setIdIpress(ipress.getIdIpress());
            dto.setCodigoRenipress(ipress.getCodigoRenipress());
            dto.setNombreIpress(ipress.getNombreIpress());
            dto.setCategoriaIpress(ipress.getCategoriaIpress());
            dto.setCodigoUbigeo(ipress.getCodigoUbigeo());
            dto.setDistrito(ipress.getDistrito());
            dto.setProvincia(ipress.getProvincia());
            dto.setDepartamento(ipress.getDepartamento());

            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public void registrar(@RequestBody IpressDTO dto) {
        Ipress ipress = new Ipress();

        ipress.setIdIpress(dto.getIdIpress());
        ipress.setCodigoRenipress(dto.getCodigoRenipress());
        ipress.setNombreIpress(dto.getNombreIpress());
        ipress.setCategoriaIpress(dto.getCategoriaIpress());
        ipress.setCodigoUbigeo(dto.getCodigoUbigeo());
        ipress.setDistrito(dto.getDistrito());
        ipress.setProvincia(dto.getProvincia());
        ipress.setDepartamento(dto.getDepartamento());

        ipressService.add(ipress);
    }

    @GetMapping("/{id}")
    public IpressDTO listarId(@PathVariable("id") Long id) {
        Ipress ipress = ipressService.listId(id);

        IpressDTO dto = new IpressDTO();

        dto.setIdIpress(ipress.getIdIpress());
        dto.setCodigoRenipress(ipress.getCodigoRenipress());
        dto.setNombreIpress(ipress.getNombreIpress());
        dto.setCategoriaIpress(ipress.getCategoriaIpress());
        dto.setCodigoUbigeo(ipress.getCodigoUbigeo());
        dto.setDistrito(ipress.getDistrito());
        dto.setProvincia(ipress.getProvincia());
        dto.setDepartamento(ipress.getDepartamento());

        return dto;
    }

    @PutMapping
    public void modificar(@RequestBody IpressDTO dto) {
        Ipress ipress = new Ipress();

        ipress.setIdIpress(dto.getIdIpress());
        ipress.setCodigoRenipress(dto.getCodigoRenipress());
        ipress.setNombreIpress(dto.getNombreIpress());
        ipress.setCategoriaIpress(dto.getCategoriaIpress());
        ipress.setCodigoUbigeo(dto.getCodigoUbigeo());
        ipress.setDistrito(dto.getDistrito());
        ipress.setProvincia(dto.getProvincia());
        ipress.setDepartamento(dto.getDepartamento());

        ipressService.modificar(ipress);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") Long id) {
        ipressService.eliminar(id);
    }
}