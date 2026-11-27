// Regras de consulta dos usuários disponíveis para a aplicação.
package br.uel.avaliacoes.service;
import java.util.List;
import org.springframework.stereotype.Service;
import br.uel.avaliacoes.dao.UsuarioDAO;
import br.uel.avaliacoes.model.Usuario;
@Service
public class UsuarioService {
    private final UsuarioDAO dao;
    public UsuarioService(UsuarioDAO dao){this.dao=dao;}
    public Usuario identificar(int registro){
        Usuario usuario=dao.buscarPorRegistro(registro);
        if(usuario==null) throw new IllegalArgumentException("Registro não encontrado.");
        return usuario;
    }
    public List<Usuario> listar(String papel){return dao.listar(papel);}
}
