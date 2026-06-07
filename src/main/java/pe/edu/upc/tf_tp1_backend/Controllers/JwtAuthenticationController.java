package pe.edu.upc.tf_tp1_backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.upc.tf_tp1_backend.Entities.Usuario;
import pe.edu.upc.tf_tp1_backend.Repositories.IUsuarioRepository;
import pe.edu.upc.tf_tp1_backend.ServiceImplements.JwtUserDetailsService;
import pe.edu.upc.tf_tp1_backend.Securities.JwtRequest;
import pe.edu.upc.tf_tp1_backend.Securities.JwtResponse;
import pe.edu.upc.tf_tp1_backend.Securities.JwtTokenUtil;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest req) throws Exception {

        authenticate(req.getCorreo(), req.getContrasena());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(req.getCorreo());

        final String token = jwtTokenUtil.generateToken(userDetails);

        Usuario usuario = usuarioRepository.findByCorreo(req.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String rol = "SIN_ROL";
        Long idIpress = null;
        String nombreIpress = "Sin IPRESS asignada";

        if (usuario.getRol() != null) {
            rol = usuario.getRol().getNombreRol();
        }

        if (usuario.getIpress() != null) {
            idIpress = usuario.getIpress().getIdIpress();
            nombreIpress = usuario.getIpress().getNombreIpress();
        }

        JwtResponse response = new JwtResponse(
                token,
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getCorreo(),
                rol,
                idIpress,
                nombreIpress
        );

        return ResponseEntity.ok(response);
    }

    private void authenticate(String correo, String contrasena) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, contrasena)
            );
        } catch (DisabledException e) {
            throw new Exception("USUARIO_INACTIVO", e);
        } catch (BadCredentialsException e) {
            throw new Exception("CREDENCIALES_INVALIDAS", e);
        }
    }
}