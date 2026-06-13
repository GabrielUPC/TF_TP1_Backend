package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FilaArchivoHospitalario {

    private final int numeroFila;
    private final Map<String, String> valores;

    public FilaArchivoHospitalario(int numeroFila, Map<String, String> valores) {
        this.numeroFila = numeroFila;
        this.valores = Collections.unmodifiableMap(new LinkedHashMap<>(valores));
    }

    public int getNumeroFila() {
        return numeroFila;
    }

    public String get(String columna) {
        return valores.get(columna);
    }

    public boolean estaVacia() {
        return valores.values().stream()
                .allMatch(valor -> valor == null || valor.isBlank());
    }
}
