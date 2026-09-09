package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloDatosHospitalariosDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionBinariaResponseDTO;
import pe.edu.upc.tf_tp1_backend.DTOS.ModeloPrediccionRequestDTO;
import pe.edu.upc.tf_tp1_backend.Enums.NivelRiesgoPresentacion;
import pe.edu.upc.tf_tp1_backend.Exceptions.PrediccionBinariaAutomaticaException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ModeloPredictivoClientServiceTest {

    private HttpServer servidor;
    private String baseUrl;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.start();
        baseUrl = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    @Test
    void solicitudNullSeRechazaAntesDeFastApi() {
        PrediccionBinariaAutomaticaException error = assertThrows(
                PrediccionBinariaAutomaticaException.class,
                () -> new ModeloPredictivoClientService(baseUrl).predecirBinario(null));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("SOLICITUD_INVALIDA", error.getCodigo());
    }

    @Test
    void enviaRequestAlEndpointBinarioYMapeaRespuesta() {
        AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
        servidor.createContext("/prediccion-binaria", intercambio -> {
            cuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            responder(intercambio, 200, respuestaBinaria(0.78, 0.36, true));
        });

        ModeloPrediccionBinariaResponseDTO respuesta =
                new ModeloPredictivoClientService(baseUrl).predecirBinario(solicitud());

        assertEquals(0.78, respuesta.getProbabilidadAltaPresion());
        assertEquals(0.36, respuesta.getThreshold());
        assertTrue(respuesta.getAlertaAltaPresion());
        assertEquals(NivelRiesgoPresentacion.ALTO, respuesta.getNivelRiesgo());
        assertEquals(0.20, respuesta.getUmbralBajoMedio());
        assertEquals(NivelRiesgoPresentacion.ALTO.getDescripcion(), respuesta.getDescripcionRiesgo());
        assertEquals("2024-07", respuesta.getPeriodoPredicho());
        assertTrue(cuerpoRecibido.get().contains("\"registro_actual\""));
        assertTrue(cuerpoRecibido.get().contains("\"historial_ultimos_meses\""));
    }

    @Test
    void serializaJsonRealConCincoHistoricosYSinIpressPublica() throws Exception {
        AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
        AtomicReference<String> metodoRecibido = new AtomicReference<>();
        AtomicReference<String> contentTypeRecibido = new AtomicReference<>();
        AtomicReference<String> contentLengthRecibido = new AtomicReference<>();
        servidor.createContext("/prediccion-binaria", intercambio -> {
            metodoRecibido.set(intercambio.getRequestMethod());
            contentTypeRecibido.set(intercambio.getRequestHeaders().getFirst("Content-Type"));
            contentLengthRecibido.set(intercambio.getRequestHeaders().getFirst("Content-Length"));
            cuerpoRecibido.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            responder(intercambio, 200, respuestaBinaria(0.30, 0.36, false));
        });

        new ModeloPredictivoClientService(baseUrl).predecirBinario(solicitudConHistorial());

        assertEquals("POST", metodoRecibido.get());
        assertEquals("application/json", contentTypeRecibido.get());
        assertNotNull(cuerpoRecibido.get());
        assertFalse(cuerpoRecibido.get().isBlank());
        assertTrue(cuerpoRecibido.get().getBytes(StandardCharsets.UTF_8).length > 0);
        if (contentLengthRecibido.get() != null) {
            assertEquals(
                    cuerpoRecibido.get().getBytes(StandardCharsets.UTF_8).length,
                    Integer.parseInt(contentLengthRecibido.get())
            );
        }
        JsonNode json = new ObjectMapper().readTree(cuerpoRecibido.get());
        assertTrue(json.has("registro_actual"));
        assertEquals(5, json.get("historial_ultimos_meses").size());
        assertEquals("00006207", json.get("registro_actual").get("codigo_ipress").asText());
        assertEquals("HOSPITALIZACION GENERAL",
                json.get("registro_actual").get("servicio_hospitalizacion").asText());
        assertFalse(json.has("co_ipress"));
    }

    @Test
    void propagaDatosInsuficientesComo422Controlado() {
        servidor.createContext("/prediccion-binaria", intercambio -> responder(
                intercambio,
                422,
                "{\"detail\":{\"estado\":\"datos_insuficientes\",\"mensaje\":\"faltan meses\"}}"
        ));

        PrediccionBinariaAutomaticaException error = assertThrows(
                PrediccionBinariaAutomaticaException.class,
                () -> new ModeloPredictivoClientService(baseUrl).predecirBinario(solicitud())
        );

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, error.getStatus());
        assertEquals("MODELO_RECHAZO_SOLICITUD", error.getCodigo());
        assertTrue(error.getMessage().contains("datos_insuficientes"));
    }

    @Test
    void rechazaContratoBinarioIncompatible() {
        servidor.createContext("/prediccion-binaria", intercambio ->
                responder(intercambio, 200, respuestaBinaria(0.78, 0.40, true)));

        PrediccionBinariaAutomaticaException error = assertThrows(
                PrediccionBinariaAutomaticaException.class,
                () -> new ModeloPredictivoClientService(baseUrl).predecirBinario(solicitud())
        );

        assertEquals(HttpStatus.BAD_GATEWAY, error.getStatus());
        assertEquals("RESPUESTA_MODELO_INCONSISTENTE", error.getCodigo());
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, false, BAJO",
            "0.199999, false, BAJO",
            "0.20, false, MEDIO",
            "0.359999, false, MEDIO",
            "0.35996, false, MEDIO",
            "0.36, true, ALTO",
            "1.0, true, ALTO"
    })
    void clasificaConValorRealSinRedondear(double probabilidad, boolean alerta, String nivel) {
        servidor.createContext("/prediccion-binaria", intercambio ->
                responder(intercambio, 200, respuestaBinaria(probabilidad, 0.36, alerta)));

        ModeloPrediccionBinariaResponseDTO respuesta =
                new ModeloPredictivoClientService(baseUrl).predecirBinario(solicitud());

        assertEquals(NivelRiesgoPresentacion.valueOf(nivel), respuesta.getNivelRiesgo());
        assertEquals(alerta, respuesta.getAlertaAltaPresion());
    }

    @ParameterizedTest
    @CsvSource({
            "0.36, false",
            "0.10, true",
            "0.30, true",
            "-0.01, false",
            "1.01, true"
    })
    void rechazaProbabilidadOAlertaInconsistente(double probabilidad, boolean alerta) {
        servidor.createContext("/prediccion-binaria", intercambio ->
                responder(intercambio, 200, respuestaBinaria(probabilidad, 0.36, alerta)));

        PrediccionBinariaAutomaticaException error = assertThrows(
                PrediccionBinariaAutomaticaException.class,
                () -> new ModeloPredictivoClientService(baseUrl).predecirBinario(solicitud())
        );

        assertEquals("RESPUESTA_MODELO_INCONSISTENTE", error.getCodigo());
        assertEquals(HttpStatus.BAD_GATEWAY, error.getStatus());
    }

    @Test
    void noCorrigeSilenciosamenteUnNivelRemotoIncoherente() {
        servidor.createContext("/prediccion-binaria", intercambio -> responder(
                intercambio,
                200,
                respuestaBinaria(0.80, 0.36, true).replace(
                        "}", ",\"nivel_riesgo\":\"BAJO\",\"umbral_bajo_medio\":0.20}"
                )
        ));

        PrediccionBinariaAutomaticaException error = assertThrows(
                PrediccionBinariaAutomaticaException.class,
                () -> new ModeloPredictivoClientService(baseUrl).predecirBinario(solicitud())
        );

        assertEquals("RESPUESTA_MODELO_INCONSISTENTE", error.getCodigo());
    }

    private static ModeloPrediccionRequestDTO solicitud() {
        ModeloDatosHospitalariosDTO actual = new ModeloDatosHospitalariosDTO();
        actual.setAnio(2024);
        actual.setMes(6);
        actual.setCodigoIpress("00006207");
        actual.setServicioHospitalizacion("HOSPITALIZACION GENERAL");
        ModeloPrediccionRequestDTO solicitud = new ModeloPrediccionRequestDTO();
        solicitud.setRegistroActual(actual);
        solicitud.setHistorialUltimosMeses(List.of());
        return solicitud;
    }

    private static ModeloPrediccionRequestDTO solicitudConHistorial() {
        ModeloPrediccionRequestDTO solicitud = solicitud();
        solicitud.setHistorialUltimosMeses(List.of(
                datos(2024, 1), datos(2024, 2), datos(2024, 3),
                datos(2024, 4), datos(2024, 5)
        ));
        return solicitud;
    }

    private static ModeloDatosHospitalariosDTO datos(int anio, int mes) {
        ModeloDatosHospitalariosDTO d = new ModeloDatosHospitalariosDTO();
        d.setAnio(anio); d.setMes(mes); d.setUbigeo("150101"); d.setDepartamento("LIMA");
        d.setProvincia("LIMA"); d.setDistrito("LIMA"); d.setSector("MINSA");
        d.setCategoriaIpress("II-2"); d.setCodigoIpress("00006207");
        d.setIdHospitalizacion("241800");
        d.setServicioHospitalizacion("HOSPITALIZACION GENERAL");
        d.setTotalIngresos(20.0); d.setTotalEgresos(18.0); d.setTotalEstancias(120.0);
        d.setTotalPacientesCamas(240.0); d.setTotalCamas(10.0);
        d.setTotalCamasDisponibles(300.0); d.setTotalFallecidos(1.0);
        return d;
    }

    private static String respuestaBinaria(double probabilidad, double threshold, boolean alerta) {
        return "{\"probabilidad_alta_presion\":" + probabilidad
                + ",\"threshold\":" + threshold
                + ",\"alerta_alta_presion\":" + alerta
                + ",\"codigo_ipress\":\"00006207\""
                + ",\"servicio_hospitalizacion\":\"HOSPITALIZACION GENERAL\""
                + ",\"periodo_actual\":\"2024-06\""
                + ",\"periodo_predicho\":\"2024-07\""
                + ",\"horizonte_prediccion\":\"mes_siguiente\""
                + ",\"modelo\":\"modelo_ipress_binario_v1\"}";
    }

    private static void responder(HttpExchange intercambio, int estado, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json");
        intercambio.sendResponseHeaders(estado, bytes.length);
        intercambio.getResponseBody().write(bytes);
        intercambio.close();
    }
}
