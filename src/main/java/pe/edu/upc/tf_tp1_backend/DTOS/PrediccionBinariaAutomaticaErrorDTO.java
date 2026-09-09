package pe.edu.upc.tf_tp1_backend.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PrediccionBinariaAutomaticaErrorDTO {
    private final String estado = "sin_prediccion";
    private final String codigo;
    private final String mensaje;

    @JsonProperty("periodos_faltantes")
    private final List<String> periodosFaltantes;

    public PrediccionBinariaAutomaticaErrorDTO(
            String codigo,
            String mensaje,
            List<String> periodosFaltantes
    ) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.periodosFaltantes = periodosFaltantes == null ? List.of() : List.copyOf(periodosFaltantes);
    }

    public String getEstado() { return estado; }
    public String getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }
    public List<String> getPeriodosFaltantes() { return periodosFaltantes; }
}
