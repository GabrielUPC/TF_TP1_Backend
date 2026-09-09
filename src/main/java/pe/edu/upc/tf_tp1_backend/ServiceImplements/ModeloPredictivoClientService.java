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
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionBinariaResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionResponseDTO;
import pe.edu.upc.tf_tp1_backend.Enums.NivelRiesgoPresentacion;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;

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
                    error.getStatusCode().value() == 422 ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.BAD_GATEWAY,
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

    public ModeloPrediccionBinariaResponseDTO predecirBinario(
            ModeloPrediccionRequestDTO solicitud
    ) {
        if (solicitud == null || solicitud.getRegistroActual() == null) {
            throw new PrediccionBinariaAutomaticaException(
                    HttpStatus.BAD_REQUEST,
                    "SOLICITUD_INVALIDA",
                    "La solicitud binaria y registro_actual son obligatorios."
            );
        }
        try {
            ModeloPrediccionBinariaResponseDTO respuesta = restClient.post()
                    .uri("/prediccion-binaria")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .body(ModeloPrediccionBinariaResponseDTO.class);

            if (respuesta == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "El microservicio binario devolvio una respuesta vacia"
                );
            }
            validarRespuestaBinaria(respuesta);
            completarNivelRiesgo(respuesta);
            return respuesta;
        } catch (RestClientResponseException error) {
            String detalle = limitarDetalle(error.getResponseBodyAsString());
            HttpStatus estado = switch (error.getStatusCode().value()) {
                case 422 -> HttpStatus.UNPROCESSABLE_CONTENT;
                case 503 -> HttpStatus.SERVICE_UNAVAILABLE;
                default -> HttpStatus.BAD_GATEWAY;
            };
            throw new ResponseStatusException(
                    estado,
                    "El microservicio binario rechazo la prediccion con estado "
                            + error.getStatusCode().value()
                            + (detalle.isBlank() ? "" : ": " + detalle),
                    error
            );
        } catch (ResourceAccessException error) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo conectar con el microservicio del modelo en /prediccion-binaria",
                    error
            );
        } catch (RestClientException error) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Fallo la comunicacion con el microservicio binario",
                    error
            );
        }
    }

    private static void validarRespuestaBinaria(ModeloPrediccionBinariaResponseDTO respuesta) {
        Double probabilidad = respuesta.getProbabilidadAltaPresion();
        Double threshold = respuesta.getThreshold();
        Boolean alerta = respuesta.getAlertaAltaPresion();
        if (probabilidad == null || !Double.isFinite(probabilidad)
                || probabilidad < 0.0 || probabilidad > 1.0
                || threshold == null
                || Double.compare(threshold, NivelRiesgoPresentacion.UMBRAL_ALTO) != 0
                || alerta == null || alerta != (probabilidad >= threshold)) {
            throw new PrediccionBinariaAutomaticaException(
                    HttpStatus.BAD_GATEWAY,
                    "RESPUESTA_MODELO_INCONSISTENTE",
                    "El microservicio binario devolvio una respuesta inconsistente."
            );
        }
    }

    private static void completarNivelRiesgo(ModeloPrediccionBinariaResponseDTO respuesta) {
        NivelRiesgoPresentacion nivel = NivelRiesgoPresentacion.desdeProbabilidad(
                respuesta.getProbabilidadAltaPresion());
        boolean alertaEsperada = nivel == NivelRiesgoPresentacion.ALTO;
        boolean nivelRemotoIncoherente = respuesta.getNivelRiesgo() != null
                && respuesta.getNivelRiesgo() != nivel;
        boolean umbralRemotoIncoherente = respuesta.getUmbralBajoMedio() != null
                && Double.compare(
                        respuesta.getUmbralBajoMedio(),
                        NivelRiesgoPresentacion.UMBRAL_BAJO_MEDIO
                ) != 0;
        if (respuesta.getAlertaAltaPresion() != alertaEsperada
                || nivelRemotoIncoherente || umbralRemotoIncoherente) {
            throw new PrediccionBinariaAutomaticaException(
                    HttpStatus.BAD_GATEWAY,
                    "RESPUESTA_MODELO_INCONSISTENTE",
                    "El nivel de riesgo no coincide con la alerta del modelo."
            );
        }
        respuesta.setNivelRiesgo(nivel);
        respuesta.setDescripcionRiesgo(nivel.getDescripcion());
        respuesta.setUmbralBajoMedio(NivelRiesgoPresentacion.UMBRAL_BAJO_MEDIO);
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
