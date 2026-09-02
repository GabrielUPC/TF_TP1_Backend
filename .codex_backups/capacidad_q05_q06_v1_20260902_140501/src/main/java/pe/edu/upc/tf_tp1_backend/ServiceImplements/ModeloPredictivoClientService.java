package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionRequestDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionResponseDTO;

@Service
public class ModeloPredictivoClientService {

    private static final int LIMITE_DETALLE_ERROR = 500;

    private final RestClient restClient;

    public ModeloPredictivoClientService(
            @Value("${modelo.ipress.url}") String modeloIpressUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(normalizarUrl(modeloIpressUrl))
                .build();
    }

    public ModeloPrediccionResponseDTO predecir(ModeloPrediccionRequestDTO solicitud) {
        try {
            ModeloPrediccionResponseDTO respuesta = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .body(ModeloPrediccionResponseDTO.class);

            if (respuesta == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "El microservicio del modelo devolvio una respuesta vacia"
                );
            }

            return respuesta;
        } catch (RestClientResponseException error) {
            String detalle = limitarDetalle(error.getResponseBodyAsString());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El microservicio del modelo rechazo la prediccion con estado "
                            + error.getStatusCode().value()
                            + (detalle.isBlank() ? "" : ": " + detalle),
                    error
            );
        } catch (ResourceAccessException error) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo conectar con el microservicio del modelo en /predict",
                    error
            );
        } catch (RestClientException error) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Fallo la comunicacion con el microservicio del modelo",
                    error
            );
        }
    }

    private static String normalizarUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("La propiedad modelo.ipress.url es obligatoria");
        }
        return url.replaceAll("/+$", "");
    }

    private String limitarDetalle(String detalle) {
        if (detalle == null) {
            return "";
        }
        String limpio = detalle.trim();
        return limpio.length() <= LIMITE_DETALLE_ERROR
                ? limpio
                : limpio.substring(0, LIMITE_DETALLE_ERROR);
    }
}
