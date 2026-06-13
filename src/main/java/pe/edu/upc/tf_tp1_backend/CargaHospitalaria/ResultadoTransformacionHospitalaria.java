package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import pe.edu.upc.tf_tp1_backend.DTOS.ErrorValidacionDTO;

import java.util.ArrayList;
import java.util.List;

public class ResultadoTransformacionHospitalaria {

    private List<RegistroHospitalarioImportado> registros = new ArrayList<>();
    private final List<ErrorValidacionDTO> errores = new ArrayList<>();
    private final List<String> advertencias = new ArrayList<>();
    private int totalFilasInvalidas;
    private int filasCoincidentesIpress;
    private int filasOmitidasOtraIpress;

    public List<RegistroHospitalarioImportado> getRegistros() {
        return registros;
    }

    public void setRegistros(List<RegistroHospitalarioImportado> registros) {
        this.registros = new ArrayList<>(registros);
    }

    public List<ErrorValidacionDTO> getErrores() {
        return errores;
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }

    public int getTotalFilasInvalidas() {
        return totalFilasInvalidas;
    }

    public void incrementarFilasInvalidas() {
        totalFilasInvalidas++;
    }

    public void agregarFilasInvalidas(int cantidad) {
        totalFilasInvalidas += cantidad;
    }

    public int getFilasCoincidentesIpress() {
        return filasCoincidentesIpress;
    }

    public void incrementarFilasCoincidentesIpress() {
        filasCoincidentesIpress++;
    }

    public int getFilasOmitidasOtraIpress() {
        return filasOmitidasOtraIpress;
    }

    public void incrementarFilasOmitidasOtraIpress() {
        filasOmitidasOtraIpress++;
    }
}
