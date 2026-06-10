package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upc.tf_tp1_backend.DTOS.ResumenCargaExcelDTO;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IExcelHospitalarioInterfaces;

@RestController
@RequestMapping("/excel-hospitalario")
@CrossOrigin(origins = "http://localhost:4200")
public class ExcelHospitalarioController {

    @Autowired
    private IExcelHospitalarioInterfaces eS;

    @GetMapping("/plantilla")
    public ResponseEntity<byte[]> descargarPlantilla() {

        byte[] archivo = eS.generarPlantillaExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla_hospitalizacion_ipress.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivo);
    }

    @PostMapping(value = "/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumenCargaExcelDTO cargarExcel(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("idUsuario") Long idUsuario,
            @RequestParam("idIpress") Long idIpress,
            Authentication authentication
    ) {
        return eS.cargarValidarYProcesarExcel(
                archivo,
                idUsuario,
                idIpress,
                authentication.getName()
        );
    }
}
