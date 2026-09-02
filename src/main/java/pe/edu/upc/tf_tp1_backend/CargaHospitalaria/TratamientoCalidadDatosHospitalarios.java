package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class TratamientoCalidadDatosHospitalarios {

    public static final String VERSION_POLITICA = "capacidad_q05_q06_v1";

    public ResultadoTratamientoCalidad aplicar(List<RegistroHospitalarioImportado> registros) {
        List<HallazgoCalidadImportado> hallazgos = new ArrayList<>();
        Set<String> gruposPendientes = new HashSet<>();
        for (RegistroHospitalarioImportado registro : registros) {
            boolean actividad = positivo(registro.getIngresos())
                    || positivo(registro.getEgresos())
                    || positivo(registro.getEstancias())
                    || positivo(registro.getPacientesCama());
            boolean q05 = cero(registro.getCamasTotales()) && actividad;
            Integer diasCama = registro.getTotalCamasDisponibles() != null
                    ? registro.getTotalCamasDisponibles()
                    : registro.getCamasDisponiblesHabilitadas();
            boolean q06 = positivo(registro.getPacientesCama()) && cero(diasCama);
            if (q05 || q06) {
                gruposPendientes.add(clave(registro));
            }
            if (q05) {
                hallazgos.add(hallazgo(registro, "Q05", "Cero camas con actividad hospitalaria."));
            }
            if (q06) {
                hallazgos.add(hallazgo(registro, "Q06", "Pacientes-dia positivos con cero dias-cama disponibles."));
            }
        }
        List<RegistroHospitalarioImportado> validos = registros.stream()
                .filter(r -> !gruposPendientes.contains(clave(r)))
                .toList();
        List<RegistroHospitalarioImportado> pendientes = registros.stream()
                .filter(r -> gruposPendientes.contains(clave(r)))
                .toList();
        return new ResultadoTratamientoCalidad(
                validos,
                pendientes,
                hallazgos,
                gruposPendientes.size()
        );
    }

    private HallazgoCalidadImportado hallazgo(RegistroHospitalarioImportado r, String regla, String descripcion) {
        return new HallazgoCalidadImportado(r.getNumeroFila(), r.getCodigoIpress(), r.getAnio(), r.getMes(),
                normalizar(r.getServicioHospitalario()), regla, descripcion);
    }

    private String clave(RegistroHospitalarioImportado r) {
        return normalizar(r.getCodigoIpress()) + "|" + r.getAnio() + "|" + r.getMes()
                + "|" + normalizar(r.getServicioHospitalario());
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private boolean positivo(Integer valor) { return valor != null && valor > 0; }
    private boolean cero(Integer valor) { return valor != null && valor == 0; }
}
