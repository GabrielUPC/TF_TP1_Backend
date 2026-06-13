package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.springframework.stereotype.Component;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;

@Component
public class TransformadorFormatoInterno {

    public ResultadoTransformacionHospitalaria transformar(
            ContenidoArchivoHospitalario contenido,
            Ipress ipress
    ) {
        ResultadoTransformacionHospitalaria resultado =
                new ResultadoTransformacionHospitalaria();

        for (FilaArchivoHospitalario fila : contenido.getFilas()) {
            String codigoIpress = TransformacionHospitalariaUtil.texto(
                    fila.get("codigo_ipress")
            );
            if (!TransformacionHospitalariaUtil.coincideIpress(
                    codigoIpress,
                    ipress.getCodigoRenipress()
            )) {
                resultado.incrementarFilasOmitidasOtraIpress();
                continue;
            }
            resultado.incrementarFilasCoincidentesIpress();

            Integer anio = TransformacionHospitalariaUtil.enteroNoNegativo(
                    fila.get("anio")
            );
            Integer mes = TransformacionHospitalariaUtil.enteroNoNegativo(
                    fila.get("mes")
            );
            String servicio = TransformacionHospitalariaUtil.texto(
                    fila.get("servicio_hospitalario")
            );
            Integer ingresos = numero(fila, "ingresos");
            Integer egresos = numero(fila, "egresos");
            Integer estancias = numero(fila, "estancias");
            Integer pacientesCama = numero(fila, "pacientes_cama");
            Integer camasTotales = numero(fila, "camas_totales");
            Integer camasDisponibles = numero(
                    fila,
                    "camas_disponibles_habilitadas"
            );

            boolean invalida = !TransformacionHospitalariaUtil.periodoValido(
                    anio,
                    mes
            )
                    || servicio.isBlank()
                    || ingresos == null
                    || egresos == null
                    || estancias == null
                    || pacientesCama == null
                    || camasTotales == null
                    || camasDisponibles == null
                    || camasDisponibles > camasTotales;

            if (invalida) {
                resultado.incrementarFilasInvalidas();
                TransformacionHospitalariaUtil.agregarError(
                        resultado.getErrores(),
                        fila.getNumeroFila(),
                        "registro",
                        "FILA_INVALIDA",
                        "La fila contiene un periodo, servicio o valor numerico invalido.",
                        "Revise los campos obligatorios y que las camas disponibles no superen las camas totales."
                );
                continue;
            }

            RegistroHospitalarioImportado registro =
                    new RegistroHospitalarioImportado();
            registro.setNumeroFila(fila.getNumeroFila());
            registro.setCodigoIpress(codigoIpress);
            registro.setAnio(anio);
            registro.setMes(mes);
            registro.setServicioHospitalario(servicio);
            registro.setIdHospitalizacion(servicio);
            registro.setIngresos(ingresos);
            registro.setEgresos(egresos);
            registro.setEstancias(estancias);
            registro.setPacientesCama(pacientesCama);
            registro.setCamasTotales(camasTotales);
            registro.setCamasDisponiblesHabilitadas(camasDisponibles);
            registro.setFallecidos(0);
            resultado.getRegistros().add(registro);
        }

        agregarAdvertenciaIpress(resultado);
        return resultado;
    }

    private Integer numero(FilaArchivoHospitalario fila, String columna) {
        return TransformacionHospitalariaUtil.enteroNoNegativo(
                fila.get(columna)
        );
    }

    private void agregarAdvertenciaIpress(
            ResultadoTransformacionHospitalaria resultado
    ) {
        if (resultado.getFilasOmitidasOtraIpress() > 0) {
            resultado.getAdvertencias().add(
                    "Se omitieron "
                            + resultado.getFilasOmitidasOtraIpress()
                            + " filas pertenecientes a otra IPRESS."
            );
        }
    }
}
