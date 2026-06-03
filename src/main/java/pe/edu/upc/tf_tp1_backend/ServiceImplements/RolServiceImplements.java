package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upc.tf_tp1_backend.Entities.Rol;
import pe.edu.upc.tf_tp1_backend.Repositories.IRolRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IRolInterfaces;

import java.util.List;

@Service
public class RolServiceImplements implements IRolInterfaces {

    @Autowired
    private IRolRepository rolRepository;

    @Override
    public List<Rol> list() {
        return rolRepository.findAll();
    }

    @Override
    public void add(Rol rol) {
        rolRepository.save(rol);
    }

    @Override
    public Rol listId(Long id) {
        return rolRepository.findById(id).orElse(new Rol());
    }

    @Override
    public void modificar(Rol rol) {
        rolRepository.save(rol);
    }

    @Override
    public void eliminar(Long id) {
        rolRepository.deleteById(id);
    }
}