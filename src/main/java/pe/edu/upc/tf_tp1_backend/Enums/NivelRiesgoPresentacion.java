package pe.edu.upc.tf_tp1_backend.Enums;

/**
 * Capa de presentación del resultado binario. No representa clases del modelo
 * ni intervalos de ocupación: interpreta la probabilidad de superar 90% de
 * ocupación durante el siguiente periodo.
 */
public enum NivelRiesgoPresentacion {
    BAJO("Probabilidad reducida de superar 90% de ocupación el siguiente periodo."),
    MEDIO("Probabilidad intermedia; requiere seguimiento preventivo."),
    ALTO("Probabilidad igual o superior al threshold operacional; requiere revisión prioritaria.");

    public static final double UMBRAL_BAJO_MEDIO = 0.20;
    public static final double UMBRAL_ALTO = 0.36;

    private final String descripcion;

    NivelRiesgoPresentacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static NivelRiesgoPresentacion desdeProbabilidad(double probabilidad) {
        if (!Double.isFinite(probabilidad) || probabilidad < 0.0 || probabilidad > 1.0) {
            throw new IllegalArgumentException("La probabilidad debe estar entre 0 y 1.");
        }
        if (probabilidad < UMBRAL_BAJO_MEDIO) {
            return BAJO;
        }
        if (probabilidad < UMBRAL_ALTO) {
            return MEDIO;
        }
        return ALTO;
    }
}
