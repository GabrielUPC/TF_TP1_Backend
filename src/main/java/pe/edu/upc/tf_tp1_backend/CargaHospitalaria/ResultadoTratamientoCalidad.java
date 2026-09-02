package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import java.util.List;

public record ResultadoTratamientoCalidad(
        List<RegistroHospitalarioImportado> registrosValidos,
        List<RegistroHospitalarioImportado> registrosPendientes,
        List<HallazgoCalidadImportado> hallazgos,
        int gruposPendientes
) {
}
