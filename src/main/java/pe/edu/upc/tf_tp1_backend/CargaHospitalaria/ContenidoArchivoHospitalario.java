package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import java.util.Collections;
import java.util.List;

public class ContenidoArchivoHospitalario {

    private final List<String> columnas;
    private final List<FilaArchivoHospitalario> filas;

    public ContenidoArchivoHospitalario(
            List<String> columnas,
            List<FilaArchivoHospitalario> filas
    ) {
        this.columnas = List.copyOf(columnas);
        this.filas = List.copyOf(filas);
    }

    public List<String> getColumnas() {
        return Collections.unmodifiableList(columnas);
    }

    public List<FilaArchivoHospitalario> getFilas() {
        return Collections.unmodifiableList(filas);
    }
}
