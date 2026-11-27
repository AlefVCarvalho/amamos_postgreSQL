// Consultas JDBC para usuários, professores e alunos.
package br.uel.avaliacoes.dao;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import br.uel.avaliacoes.model.Usuario;
@Repository
public class UsuarioDAO {
    private final JdbcTemplate jdbc;
    public UsuarioDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public Usuario buscarPorRegistro(int registro){
        List<Usuario> usuarios=jdbc.query("SELECT u.REGISTRO,u.NOME,CASE WHEN p.REGISTRO IS NOT NULL THEN 'PROFESSOR' WHEN a.REGISTRO IS NOT NULL THEN 'ALUNO' ELSE 'SEM_PAPEL' END AS PAPEL FROM Usuario u LEFT JOIN Professor p ON p.REGISTRO=u.REGISTRO LEFT JOIN Aluno a ON a.REGISTRO=u.REGISTRO WHERE u.REGISTRO=?",(rs,n)->new Usuario(rs.getInt("registro"),rs.getString("nome"),rs.getString("papel")),registro);
        return usuarios.isEmpty()?null:usuarios.get(0);
    }
    public List<Usuario> listar(String papel){
        String sql="SELECT u.REGISTRO,u.NOME,CASE WHEN p.REGISTRO IS NOT NULL THEN 'PROFESSOR' ELSE 'ALUNO' END AS PAPEL FROM Usuario u LEFT JOIN Professor p ON p.REGISTRO=u.REGISTRO LEFT JOIN Aluno a ON a.REGISTRO=u.REGISTRO";
        if ("PROFESSOR".equals(papel)) sql += " WHERE p.REGISTRO IS NOT NULL";
        if ("ALUNO".equals(papel)) sql += " WHERE a.REGISTRO IS NOT NULL";
        sql += " ORDER BY u.NOME";
        return jdbc.query(sql,(rs,n)->new Usuario(rs.getInt("registro"),rs.getString("nome"),rs.getString("papel")));
    }
}
