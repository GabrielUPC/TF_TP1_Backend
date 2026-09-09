package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.YearMonth;

@Component
public class TratamientoCalidadDatosHospitalarios {

    public static final String VERSION_POLITICA = "capacidad_q05_q06_q07_q08_v3";

    public ResultadoTratamientoCalidad aplicar(List<RegistroHospitalarioImportado> registros) {
        List<HallazgoCalidadImportado> hallazgos = new ArrayList<>();
        Set<String> gruposPendientes = new HashSet<>();
        for (RegistroHospitalarioImportado registro : registros) {
            boolean actividad = positivo(registro.getIngresos())
                    || positivo(registro.getEgresos())
                    || positivo(registro.getEstancias())
                    || positivo(registro.getPacientesCama());
            boolean q05 = cero(registro.getCamasTotales()) && actividad;
            Double diasCama = diasCamaDisponibles(registro);
            boolean q06 = positivo(registro.getPacientesCama()) && cero(diasCama);
            boolean q07 = esQ07(registro);
            boolean q08 = positivo(registro.getPacientesCama()) && positivo(diasCama)
                    && registro.getPacientesCama().doubleValue() / diasCama.doubleValue() > 1.20;
            if (q05 || q06 || q07 || q08) {
                gruposPendientes.add(clave(registro));
            }
            if (q05) {
                hallazgos.add(hallazgo(registro, "Q05", "Cero camas con actividad hospitalaria."));
            }
            if (q06) {
                hallazgos.add(hallazgo(registro, "Q06", "Pacientes-dia positivos con cero dias-cama disponibles."));
            }
            if (q07) {
                hallazgos.add(hallazgo(registro, "Q07",
                        "Camas = pacientes-dia por dias del mes: posible desorden de metricas; no se corrige automaticamente."));
            }
            if (q08) {
                hallazgos.add(hallazgo(registro, "Q08",
                        "Pacientes-dia / dias-cama disponibles supera 1.20."));
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
        return NormalizacionPredictiva.texto(valor);
    }

    private Double diasCamaDisponibles(RegistroHospitalarioImportado registro) {
        if (registro.getTotalCamasDisponibles() != null) {
            return registro.getTotalCamasDisponibles().doubleValue();
        }
        if (registro.getCamasDisponiblesHabilitadas() == null
                || registro.getAnio() == null || registro.getMes() == null) {
            return null;
        }
        try {
            return registro.getCamasDisponiblesHabilitadas().doubleValue()
                    * YearMonth.of(registro.getAnio(), registro.getMes()).lengthOfMonth();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private boolean positivo(Number valor) { return valor != null && valor.doubleValue() > 0; }
    private boolean cero(Number valor) { return valor != null && valor.doubleValue() == 0; }
    private boolean esQ07(RegistroHospitalarioImportado r) {
        if (!positivo(r.getPacientesCama()) || !positivo(r.getCamasTotales())
                || r.getAnio() == null || r.getMes() == null) return false;
        try {
            return r.getCamasTotales().longValue()
                    == r.getPacientesCama().longValue() * YearMonth.of(r.getAnio(), r.getMes()).lengthOfMonth();
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
