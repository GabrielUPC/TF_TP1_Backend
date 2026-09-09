package pe.edu.upc.tf_tp1_backend.Exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;

public class PrediccionBinariaAutomaticaException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;
    private final String codigo;
    private final List<String> periodosFaltantes;

    public PrediccionBinariaAutomaticaException(HttpStatus status, String codigo, String mensaje) {
        this(status, codigo, mensaje, List.of());
    }

    public PrediccionBinariaAutomaticaException(
            HttpStatus status,
            String codigo,
            String mensaje,
            List<String> periodosFaltantes
    ) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
        this.periodosFaltantes = periodosFaltantes == null ? List.of() : List.copyOf(periodosFaltantes);
    }

    public HttpStatus getStatus() { return status; }
    public String getCodigo() { return codigo; }
    public List<String> getPeriodosFaltantes() { return periodosFaltantes; }
}
