// Endpoints para consulta de professores e alunos.
package br.uel.avaliacoes.controller;
import java.util.List; import org.springframework.web.bind.annotation.*;
import br.uel.avaliacoes.model.Usuario; import br.uel.avaliacoes.service.UsuarioService;
@RestController @RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service; public UsuarioController(UsuarioService s){service=s;}
    @GetMapping public List<Usuario> listar(@RequestParam(required=false) String papel){return service.listar(papel==null?null:papel.toUpperCase());}
}
