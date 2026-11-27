// Consultas JDBC para listar as matérias disponíveis.
package br.uel.avaliacoes.dao;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import br.uel.avaliacoes.model.Materia;
@Repository
public class MateriaDAO {
    private final JdbcTemplate jdbc;
    public MateriaDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public List<Materia> listar(){return jdbc.query("SELECT SIGLA,NOME FROM Materia ORDER BY NOME",(rs,n)->new Materia(rs.getString("SIGLA"),rs.getString("NOME")));}
}
