package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ConsolidadorRegistrosHospitalarios {

    private static final double UMBRAL_DISCREPANCIA_CAPACIDAD = 0.20;

    public List<RegistroHospitalarioImportado> consolidar(
            List<RegistroHospitalarioImportado> registros,
            List<String> advertencias
    ) {
        Map<String, Acumulador> grupos = new LinkedHashMap<>();
        int duplicadosExactos = 0;

        for (RegistroHospitalarioImportado registro : registros) {
            String clave = construirClave(registro);
            Acumulador acumulador = grupos.get(clave);
            if (acumulador == null) {
                grupos.put(clave, new Acumulador(registro));
            } else if (!acumulador.agregar(registro)) {
                duplicadosExactos++;
            }
        }

        List<RegistroHospitalarioImportado> consolidados = new ArrayList<>();
        int discrepancias = 0;
        for (Acumulador acumulador : grupos.values()) {
            consolidados.add(acumulador.getRegistro());
            if (acumulador.isDiscrepanciaCapacidad()) {
                discrepancias++;
            }
        }

        if (duplicadosExactos > 0) {
            advertencias.add(
                    "Se omitieron " + duplicadosExactos + " filas duplicadas exactas."
            );
        }
        if (discrepancias > 0) {
            advertencias.add(
                    "Se detectaron "
                            + discrepancias
                            + " grupos con diferencias de capacidad superiores al 20%; se uso el valor maximo."
            );
        }
        return consolidados;
    }

    private String construirClave(RegistroHospitalarioImportado registro) {
        return TransformacionHospitalariaUtil.claveNormalizada(
                registro.getCodigoIpress()
        )
                + "|" + registro.getAnio()
                + "|" + registro.getMes()
                + "|" + TransformacionHospitalariaUtil.claveNormalizada(
                        registro.getServicioHospitalario()
                );
    }

    private static class Acumulador {

        private final RegistroHospitalarioImportado registro;
        private final Set<String> firmas = new LinkedHashSet<>();
        private int minimoCamasTotales;
        private int maximoCamasTotales;
        private int minimoCapacidadDisponible;
        private int maximoCapacidadDisponible;

        Acumulador(RegistroHospitalarioImportado inicial) {
            this.registro = new RegistroHospitalarioImportado(inicial);
            firmas.add(firma(inicial));
            minimoCamasTotales = valorCapacidad(inicial.getCamasTotales());
            maximoCamasTotales = minimoCamasTotales;
            int disponible = capacidadDisponible(inicial);
            minimoCapacidadDisponible = disponible;
            maximoCapacidadDisponible = disponible;
        }

        boolean agregar(RegistroHospitalarioImportado siguiente) {
            if (!firmas.add(firma(siguiente))) {
                return false;
            }

            registro.setIngresos(sumar(registro.getIngresos(), siguiente.getIngresos()));
            registro.setEgresos(sumar(registro.getEgresos(), siguiente.getEgresos()));
            registro.setEstancias(sumar(registro.getEstancias(), siguiente.getEstancias()));
            registro.setPacientesCama(sumar(
                    registro.getPacientesCama(),
                    siguiente.getPacientesCama()
            ));
            registro.setFallecidos(sumar(
                    registro.getFallecidos(),
                    siguiente.getFallecidos()
            ));

            // La capacidad describe el mismo servicio y periodo. Sumarla
            // duplicaria camas; se conserva el maximo como regla conservadora.
            registro.setCamasTotales(maximo(
                    registro.getCamasTotales(),
                    siguiente.getCamasTotales()
            ));
            registro.setCamasDisponiblesHabilitadas(maximo(
                    registro.getCamasDisponiblesHabilitadas(),
                    siguiente.getCamasDisponiblesHabilitadas()
            ));
            registro.setTotalCamasDisponibles(maximo(
                    registro.getTotalCamasDisponibles(),
                    siguiente.getTotalCamasDisponibles()
            ));

            actualizarRangos(siguiente);
            return true;
        }

        RegistroHospitalarioImportado getRegistro() {
            return registro;
        }

        boolean isDiscrepanciaCapacidad() {
            return discrepancia(minimoCamasTotales, maximoCamasTotales)
                    || discrepancia(
                            minimoCapacidadDisponible,
                            maximoCapacidadDisponible
                    );
        }

        private void actualizarRangos(RegistroHospitalarioImportado siguiente) {
            int camas = valorCapacidad(siguiente.getCamasTotales());
            minimoCamasTotales = minimoPositivo(minimoCamasTotales, camas);
            maximoCamasTotales = Math.max(maximoCamasTotales, camas);

            int disponible = capacidadDisponible(siguiente);
            minimoCapacidadDisponible = minimoPositivo(
                    minimoCapacidadDisponible,
                    disponible
            );
            maximoCapacidadDisponible = Math.max(
                    maximoCapacidadDisponible,
                    disponible
            );
        }

        private boolean discrepancia(int minimo, int maximo) {
            return minimo > 0
                    && maximo > minimo
                    && (maximo - minimo) / (double) minimo
                    > UMBRAL_DISCREPANCIA_CAPACIDAD;
        }

        private int capacidadDisponible(RegistroHospitalarioImportado item) {
            Integer valor = item.getTotalCamasDisponibles() != null
                    ? item.getTotalCamasDisponibles()
                    : item.getCamasDisponiblesHabilitadas();
            return valorCapacidad(valor);
        }

        private int valorCapacidad(Integer valor) {
            return valor == null ? 0 : valor;
        }

        private int minimoPositivo(int actual, int candidato) {
            if (actual == 0) {
                return candidato;
            }
            if (candidato == 0) {
                return actual;
            }
            return Math.min(actual, candidato);
        }

        private Integer sumar(Integer primero, Integer segundo) {
            return Math.addExact(
                    primero == null ? 0 : primero,
                    segundo == null ? 0 : segundo
            );
        }

        private Integer maximo(Integer primero, Integer segundo) {
            if (primero == null) {
                return segundo;
            }
            if (segundo == null) {
                return primero;
            }
            return Math.max(primero, segundo);
        }

        private String firma(RegistroHospitalarioImportado item) {
            return item.getIngresos()
                    + "|" + item.getEgresos()
                    + "|" + item.getEstancias()
                    + "|" + item.getPacientesCama()
                    + "|" + item.getCamasTotales()
                    + "|" + item.getCamasDisponiblesHabilitadas()
                    + "|" + item.getTotalCamasDisponibles()
                    + "|" + item.getFallecidos();
        }
    }
}
