package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionBinariaResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.CatalogoPrediccionBinariaDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionBinariaAutomaticaErrorDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionBinariaAutomaticaRequestDTO;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;
import pe.edu.upc.tf_tp1_backend.ServiceImplements.PrediccionBinariaAutomaticaService;

@RestController
@RequestMapping("/predicciones-riesgo/binaria")
@CrossOrigin(origins = "http://localhost:4200")
public class PrediccionBinariaAutomaticaController {
    private final PrediccionBinariaAutomaticaService servicio;

    public PrediccionBinariaAutomaticaController(PrediccionBinariaAutomaticaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/automatica")
    public ModeloPrediccionBinariaResponseDTO predecir(
            @RequestBody(required = false) PrediccionBinariaAutomaticaRequestDTO solicitud,
            Authentication authentication
    ) {
        return servicio.predecir(solicitud, authentication.getName());
    }

    @GetMapping("/catalogo")
    public CatalogoPrediccionBinariaDTO catalogo(Authentication authentication) {
        return servicio.catalogo(authentication.getName());
    }

    @ExceptionHandler(PrediccionBinariaAutomaticaException.class)
    public ResponseEntity<PrediccionBinariaAutomaticaErrorDTO> manejarError(
            PrediccionBinariaAutomaticaException error
    ) {
        return ResponseEntity.status(error.getStatus()).body(
                new PrediccionBinariaAutomaticaErrorDTO(
                        error.getCodigo(), error.getMessage(), error.getPeriodosFaltantes()
                )
        );
    }
}
