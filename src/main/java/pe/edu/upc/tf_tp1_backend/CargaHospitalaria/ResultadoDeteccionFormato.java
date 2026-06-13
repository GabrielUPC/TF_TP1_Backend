package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import java.util.List;

public class ResultadoDeteccionFormato {

    private final FormatoArchivoHospitalario formato;
    private final List<String> columnasEncontradas;
    private final List<String> columnasMinimasFormatoInterno;
    private final List<String> columnasMinimasDatasetD1;

    public ResultadoDeteccionFormato(
            FormatoArchivoHospitalario formato,
            List<String> columnasEncontradas,
            List<String> columnasMinimasFormatoInterno,
            List<String> columnasMinimasDatasetD1
    ) {
        this.formato = formato;
        this.columnasEncontradas = List.copyOf(columnasEncontradas);
        this.columnasMinimasFormatoInterno =
                List.copyOf(columnasMinimasFormatoInterno);
        this.columnasMinimasDatasetD1 = List.copyOf(columnasMinimasDatasetD1);
    }

    public FormatoArchivoHospitalario getFormato() {
        return formato;
    }

    public List<String> getColumnasEncontradas() {
        return columnasEncontradas;
    }

    public List<String> getColumnasMinimasFormatoInterno() {
        return columnasMinimasFormatoInterno;
    }

    public List<String> getColumnasMinimasDatasetD1() {
        return columnasMinimasDatasetD1;
    }
}
