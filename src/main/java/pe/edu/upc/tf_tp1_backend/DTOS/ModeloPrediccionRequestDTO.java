package pe.edu.upc.tf_tp1_backend.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ModeloPrediccionRequestDTO {

    @JsonProperty("registro_actual")
    private ModeloDatosHospitalariosDTO registroActual;

    @JsonProperty("historial_ultimos_meses")
    private List<ModeloDatosHospitalariosDTO> historialUltimosMeses = new ArrayList<>();

    public ModeloDatosHospitalariosDTO getRegistroActual() {
        return registroActual;
    }

    public void setRegistroActual(ModeloDatosHospitalariosDTO registroActual) {
        this.registroActual = registroActual;
    }

    public List<ModeloDatosHospitalariosDTO> getHistorialUltimosMeses() {
        return historialUltimosMeses;
    }

    public void setHistorialUltimosMeses(List<ModeloDatosHospitalariosDTO> historialUltimosMeses) {
        this.historialUltimosMeses = historialUltimosMeses;
    }
}
