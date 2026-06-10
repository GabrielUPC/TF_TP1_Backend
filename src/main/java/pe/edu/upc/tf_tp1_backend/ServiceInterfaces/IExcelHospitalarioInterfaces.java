package pe.edu.upc.tf_tp1_backend.ServiceInterfaces;

import pe.edu.upc.tf_tp1_backend.DTOS.ResumenCargaExcelDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IExcelHospitalarioInterfaces {

    byte[] generarPlantillaExcel();

    ResumenCargaExcelDTO cargarValidarYProcesarExcel(
            MultipartFile archivo,
            Long idUsuario,
            Long idIpress,
            String correoUsuario
    );
}
