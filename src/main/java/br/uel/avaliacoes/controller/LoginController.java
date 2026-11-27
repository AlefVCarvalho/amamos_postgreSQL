package br.uel.avaliacoes.controller;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import br.uel.avaliacoes.model.Usuario;
import br.uel.avaliacoes.service.UsuarioService;
@RestController
@RequestMapping("/api")
public class LoginController {
    private final UsuarioService service;
    public LoginController(UsuarioService service){this.service=service;}
    @PostMapping("/login") public Map<String,Object> entrar(@RequestBody Map<String,Object> body,HttpSession session){
        Object valor=body.get("registro");
        if(valor==null) throw new IllegalArgumentException("Informe seu registro.");
        Usuario usuario=service.identificar(Integer.parseInt(valor.toString()));
        session.setAttribute("registro",usuario.getRegistro());
        session.setAttribute("papel",usuario.getPapel());
        session.setAttribute("nome",usuario.getNome());
        return Map.of("registro",usuario.getRegistro(),"nome",usuario.getNome(),"papel",usuario.getPapel());
    }
    @GetMapping("/sessao") public Map<String,Object> sessao(HttpSession session){
        if(session.getAttribute("registro")==null) throw new IllegalArgumentException("Faça a identificação para continuar.");
        return Map.of("registro",session.getAttribute("registro"),"nome",session.getAttribute("nome"),"papel",session.getAttribute("papel"));
    }
    @PostMapping("/logout") public Map<String,String> sair(HttpSession session){session.invalidate();return Map.of("mensagem","Sessão encerrada.");}
}
