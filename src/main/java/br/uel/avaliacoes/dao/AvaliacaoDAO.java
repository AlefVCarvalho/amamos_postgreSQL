// Operações JDBC para avaliações, associação de questões e designação de alunos.
package br.uel.avaliacoes.dao;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import br.uel.avaliacoes.model.Avaliacao;
@Repository
public class AvaliacaoDAO {
    private final JdbcTemplate jdbc;
    public AvaliacaoDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public List<Map<String,Object>> listar(){return jdbc.queryForList("SELECT a.CODIGO,a.TITULO,a.DESCRICAO,a.DATA,a.INICIO,a.FIM,m.NOME AS MATERIA,u.NOME AS PROFESSOR,(SELECT COUNT(*) FROM Contem c WHERE c.CODIGO=a.CODIGO) AS TOTAL_QUESTOES,(SELECT STRING_AGG(f.REGISTRO::text, ', ' ORDER BY f.REGISTRO) FROM Faz f WHERE f.CODIGO=a.CODIGO) AS ALUNOS_DESIGNADOS FROM Avaliacao a JOIN Materia m ON m.SIGLA=a.SIGLA JOIN Usuario u ON u.REGISTRO=a.REGISTRO ORDER BY a.DATA DESC,a.CODIGO DESC");}
    public List<Map<String,Object>> listarDoAluno(int registro){return jdbc.queryForList("SELECT a.CODIGO,a.TITULO,a.DESCRICAO,a.DATA,a.INICIO,a.FIM,m.NOME AS MATERIA,(SELECT COUNT(*) FROM Contem c WHERE c.CODIGO=a.CODIGO) AS TOTAL_QUESTOES FROM Avaliacao a JOIN Materia m ON m.SIGLA=a.SIGLA JOIN Faz f ON f.CODIGO=a.CODIGO WHERE f.REGISTRO=? AND f.RESPONDIDA=FALSE AND NOT EXISTS (SELECT 1 FROM Resposta r WHERE r.REGISTRO=f.REGISTRO AND r.CODIGO=f.CODIGO GROUP BY r.REGISTRO,r.CODIGO HAVING COUNT(*)=(SELECT COUNT(*) FROM Contem c WHERE c.CODIGO=a.CODIGO)) ORDER BY a.DATA,a.CODIGO",registro);}
    public int proximoCodigo(){Integer n=jdbc.queryForObject("SELECT COALESCE(MAX(CODIGO),0)+1 FROM Avaliacao",Integer.class);return n==null?1:n;}
    public void inserir(Avaliacao a){jdbc.update("INSERT INTO Avaliacao(CODIGO,TITULO,DESCRICAO,INICIO,FIM,DATA,REGISTRO,SIGLA) VALUES (?,?,?,?,?,?,?,?)",a.getCodigo(),a.getTitulo(),a.getDescricao(),a.getInicio(),a.getFim(),a.getData(),a.getRegistroProfessor(),a.getSiglaMateria());}
    public void inserirQuestao(int codigo,int numero,java.math.BigDecimal valor){jdbc.update("INSERT INTO Contem(CODIGO,NUMERO,VALOR) VALUES (?,?,?)",codigo,numero,valor);}
    public void designarAluno(int codigo,int registro){jdbc.update("INSERT INTO Faz(REGISTRO,CODIGO) VALUES (?,?)",registro,codigo);}
    public boolean alunoDesignado(int codigo,int registro){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM Faz WHERE CODIGO=? AND REGISTRO=?",Integer.class,codigo,registro);return n!=null&&n>0;}
    public Map<String,Object> obterParaAluno(int codigo,int registro){return jdbc.queryForMap("SELECT a.CODIGO,a.TITULO,a.DESCRICAO,a.DATA,a.INICIO,a.FIM,m.NOME AS MATERIA FROM Avaliacao a JOIN Materia m ON m.SIGLA=a.SIGLA JOIN Faz f ON f.CODIGO=a.CODIGO WHERE a.CODIGO=? AND f.REGISTRO=?",codigo,registro);}
}
