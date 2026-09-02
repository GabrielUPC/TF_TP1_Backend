package pe.edu.upc.tf_tp1_backend.CargaHospitalaria;

public record HallazgoCalidadImportado(
        int fila,
        String codigoIpress,
        Integer anio,
        Integer mes,
        String servicioHospitalario,
        String regla,
        String descripcion
) {
}
