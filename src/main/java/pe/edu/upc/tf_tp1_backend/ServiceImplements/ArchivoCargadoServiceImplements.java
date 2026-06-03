package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upc.tf_tp1_backend.Entities.ArchivoCargado;
import pe.edu.upc.tf_tp1_backend.Repositories.IArchivoCargadoRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IArchivoCargadoInterfaces;

import java.util.List;

@Service
public class ArchivoCargadoServiceImplements implements IArchivoCargadoInterfaces {

    @Autowired
    private IArchivoCargadoRepository archivoCargadoRepository;

    @Override
    public List<ArchivoCargado> list() {
        return archivoCargadoRepository.findAll();
    }

    @Override
    public void add(ArchivoCargado archivoCargado) {
        archivoCargadoRepository.save(archivoCargado);
    }

    @Override
    public ArchivoCargado listId(Long id) {
        return archivoCargadoRepository.findById(id).orElse(new ArchivoCargado());
    }

    @Override
    public void modificar(ArchivoCargado archivoCargado) {
        archivoCargadoRepository.save(archivoCargado);
    }

    @Override
    public void eliminar(Long id) {
        archivoCargadoRepository.deleteById(id);
    }
}