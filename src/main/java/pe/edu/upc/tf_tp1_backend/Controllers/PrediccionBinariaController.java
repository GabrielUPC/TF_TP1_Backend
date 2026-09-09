package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionBinariaResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionRequestDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.PrediccionBinariaAutomaticaErrorDTO;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;
import pe.edu.upc.tf_tp1_backend.ServiceImplements.ModeloPredictivoClientService;

@RestController
@RequestMapping("/predicciones-riesgo")
public class PrediccionBinariaController {

    private final ModeloPredictivoClientService modeloClient;

    public PrediccionBinariaController(ModeloPredictivoClientService modeloClient) {
        this.modeloClient = modeloClient;
    }

    @PostMapping("/binaria")
    public ModeloPrediccionBinariaResponseDTO predecirBinario(
            @RequestBody ModeloPrediccionRequestDTO solicitud
    ) {
        return modeloClient.predecirBinario(solicitud);
    }

    @ExceptionHandler(PrediccionBinariaAutomaticaException.class)
    public ResponseEntity<PrediccionBinariaAutomaticaErrorDTO> manejarRespuestaInconsistente(
            PrediccionBinariaAutomaticaException error
    ) {
        return ResponseEntity.status(error.getStatus()).body(
                new PrediccionBinariaAutomaticaErrorDTO(
                        error.getCodigo(), error.getMessage(), error.getPeriodosFaltantes()
                )
        );
    }
}
