package pe.edu.upc.tf_tp1_backend.ServiceImplements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upc.tf_tp1_backend.Entities.Ipress;
import pe.edu.upc.tf_tp1_backend.Repositories.IIpressRepository;
import pe.edu.upc.tf_tp1_backend.ServiceInterfaces.IIpressInterfaces;

import java.util.List;

@Service
public class IpressServiceImplements implements IIpressInterfaces {

    @Autowired
    private IIpressRepository ipressRepository;

    @Override
    public List<Ipress> list() {
        return ipressRepository.findAll();
    }

    @Override
    public void add(Ipress ipress) {
        ipressRepository.save(ipress);
    }

    @Override
    public Ipress listId(Long id) {
        return ipressRepository.findById(id).orElse(new Ipress());
    }

    @Override
    public void modificar(Ipress ipress) {
        ipressRepository.save(ipress);
    }

    @Override
    public void eliminar(Long id) {
        ipressRepository.deleteById(id);
    }
}