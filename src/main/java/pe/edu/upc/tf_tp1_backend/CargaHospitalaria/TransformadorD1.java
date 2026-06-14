package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

import org.springframework.stereotype.Component;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;

@Component
public class TransformadorD1 {

    private static final String[] COLUMNAS_NUMERICAS = {
            "nro_total_hospit_ing",
            "nro_total_hospit_egr",
            "nro_total_estancias",
            "nro_total_pacientes_camas",
            "nro_total_camas",
            "dias_cama_disponible",
            "nro_total_camas_disponib",
            "nro_total_camas_disponibles",
            "nro_total_fallecidos"
    };

    public ResultadoTransformacionHospitalaria transformar(
            ContenidoArchivoHospitalario contenido,
            Ipress ipress
    ) {
        ResultadoTransformacionHospitalaria resultado =
                new ResultadoTransformacionHospitalaria();
        int filasSinHospitalizacion = 0;

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

            String idHospitalizacion = TransformacionHospitalariaUtil.texto(
                    fila.get("id_hospitalizacion")
            );
            String servicio = TransformacionHospitalariaUtil.texto(
                    fila.get("servicio_hospitalario")
            );
            if (TransformacionHospitalariaUtil.esSinInformacion(idHospitalizacion)
                    || servicio.isBlank()
                    || contieneMarcadorNumerico(fila)) {
                resultado.incrementarFilasInvalidas();
                filasSinHospitalizacion++;
                continue;
            }

            Integer anio = TransformacionHospitalariaUtil.enteroNoNegativo(
                    fila.get("anio")
            );
            Integer mes = TransformacionHospitalariaUtil.enteroNoNegativo(
                    fila.get("mes")
            );
            Integer ingresos = numero(fila, "nro_total_hospit_ing");
            Integer egresos = numero(fila, "nro_total_hospit_egr");
            Integer estancias = numero(fila, "nro_total_estancias");
            Integer pacientesCama = numero(
                    fila,
                    "nro_total_pacientes_camas"
            );
            Integer camasTotales = numero(fila, "nro_total_camas");
            Integer camasDiaDisponibles = numeroCamasDia(fila);
            Integer fallecidos = numero(fila, "nro_total_fallecidos");

            boolean invalida = codigoIpress.isBlank()
                    || idHospitalizacion.isBlank()
                    || !TransformacionHospitalariaUtil.periodoValido(anio, mes)
                    || ingresos == null
                    || egresos == null
                    || estancias == null
                    || pacientesCama == null
                    || camasTotales == null
                    || camasDiaDisponibles == null
                    || fallecidos == null;

            if (invalida) {
                resultado.incrementarFilasInvalidas();
                TransformacionHospitalariaUtil.agregarError(
                        resultado.getErrores(),
                        fila.getNumeroFila(),
                        "registro",
                        "FILA_D1_INVALIDA",
                        "La fila D1 contiene un periodo o valor numerico invalido.",
                        "Revise las metricas D1 y elimine marcadores NE_0001."
                );
                continue;
            }

            RegistroHospitalarioImportado registro =
                    new RegistroHospitalarioImportado();
            registro.setNumeroFila(fila.getNumeroFila());
            registro.setCodigoIpress(codigoIpress);
            registro.setNombreIpress(TransformacionHospitalariaUtil.texto(
                    fila.get("razon_soc")
            ));
            registro.setCategoriaIpress(TransformacionHospitalariaUtil.texto(
                    fila.get("categoria")
            ));
            registro.setCodigoUbigeo(TransformacionHospitalariaUtil.texto(
                    fila.get("ubigeo")
            ));
            registro.setDepartamento(TransformacionHospitalariaUtil.texto(
                    fila.get("departamento")
            ));
            registro.setProvincia(TransformacionHospitalariaUtil.texto(
                    fila.get("provincia")
            ));
            registro.setDistrito(TransformacionHospitalariaUtil.texto(
                    fila.get("distrito")
            ));
            registro.setSector(TransformacionHospitalariaUtil.texto(
                    fila.get("sector")
            ));
            registro.setIdHospitalizacion(idHospitalizacion);
            registro.setServicioHospitalario(servicio);
            registro.setAnio(anio);
            registro.setMes(mes);
            registro.setIngresos(ingresos);
            registro.setEgresos(egresos);
            registro.setEstancias(estancias);
            registro.setPacientesCama(pacientesCama);
            registro.setCamasTotales(camasTotales);
            registro.setTotalCamasDisponibles(camasDiaDisponibles);
            registro.setFallecidos(fallecidos);
            resultado.getRegistros().add(registro);
        }

        if (resultado.getFilasOmitidasOtraIpress() > 0) {
            resultado.getAdvertencias().add(
                    "Se omitieron "
                            + resultado.getFilasOmitidasOtraIpress()
                            + " filas pertenecientes a otra IPRESS."
            );
        }
        if (filasSinHospitalizacion > 0) {
            resultado.getAdvertencias().add(
                    "Se descartaron "
                            + filasSinHospitalizacion
                            + " filas D1 sin informacion hospitalaria util o con NE_0001."
            );
        }
        return resultado;
    }

    private boolean contieneMarcadorNumerico(FilaArchivoHospitalario fila) {
        for (String columna : COLUMNAS_NUMERICAS) {
            if (TransformacionHospitalariaUtil.esSinInformacion(
                    fila.get(columna)
            )) {
                return true;
            }
        }
        return false;
    }

    private Integer numero(FilaArchivoHospitalario fila, String columna) {
        return TransformacionHospitalariaUtil.enteroNoNegativo(
                fila.get(columna)
        );
    }

    private Integer numeroCamasDia(FilaArchivoHospitalario fila) {
        String valor = fila.get("dias_cama_disponible");
        if (valor == null) {
            valor = fila.get("nro_total_camas_disponib");
        }
        if (valor == null) {
            valor = fila.get("nro_total_camas_disponibles");
        }
        return TransformacionHospitalariaUtil.enteroNoNegativo(valor);
    }
}
