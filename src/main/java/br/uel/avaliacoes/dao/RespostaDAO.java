// Operações JDBC para envio, correção e relatórios de respostas.
package br.uel.avaliacoes.dao;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class RespostaDAO {
    private final JdbcTemplate jdbc;
    public RespostaDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public boolean alunoDesignado(int aluno,int codigo){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM Faz WHERE REGISTRO=? AND CODIGO=?",Integer.class,aluno,codigo);return n!=null&&n>0;}
    public void bloquearDesignacao(int aluno,int codigo){jdbc.queryForObject("SELECT REGISTRO FROM Faz WHERE REGISTRO=? AND CODIGO=? FOR UPDATE",Integer.class,aluno,codigo);}
    public boolean jaRespondeu(int aluno,int codigo){
        Boolean respondida=jdbc.queryForObject("SELECT f.RESPONDIDA OR (SELECT COUNT(*) FROM Resposta r WHERE r.REGISTRO=f.REGISTRO AND r.CODIGO=f.CODIGO)>=(SELECT COUNT(*) FROM Contem c WHERE c.CODIGO=f.CODIGO) FROM Faz f WHERE f.REGISTRO=? AND f.CODIGO=?",Boolean.class,aluno,codigo);
        return Boolean.TRUE.equals(respondida);
    }
    public void marcarRespondida(int aluno,int codigo){jdbc.update("UPDATE Faz SET RESPONDIDA=TRUE WHERE REGISTRO=? AND CODIGO=?",aluno,codigo);}
    public int quantidadeQuestoes(int codigo){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM Contem WHERE CODIGO=?",Integer.class,codigo);return n==null?0:n;}
    public boolean questaoDaAvaliacao(int codigo,int numero){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM Contem WHERE CODIGO=? AND NUMERO=?",Integer.class,codigo,numero);return n!=null&&n>0;}
    public void salvar(int aluno,int codigo,int numero,String resposta){jdbc.update("INSERT INTO Resposta(REGISTRO,CODIGO,NUMERO,RESPOSTA) VALUES (?,?,?,?)",aluno,codigo,numero,resposta);}
    public void corrigir(int aluno,int codigo,int numero,BigDecimal nota){jdbc.update("UPDATE Resposta SET NOTA=? WHERE REGISTRO=? AND CODIGO=? AND NUMERO=? AND NUMERO IN (SELECT NUMERO FROM Subjetiva)",nota,aluno,codigo,numero);}
    public List<Map<String,Object>> pendentes(){return jdbc.queryForList("SELECT r.REGISTRO,u.NOME AS ALUNO,r.CODIGO,a.TITULO,r.NUMERO,q.ENUNCIADO,r.RESPOSTA,c.VALOR FROM Resposta r JOIN Subjetiva s ON s.NUMERO=r.NUMERO JOIN Usuario u ON u.REGISTRO=r.REGISTRO JOIN Avaliacao a ON a.CODIGO=r.CODIGO JOIN Questao q ON q.NUMERO=r.NUMERO JOIN Contem c ON c.CODIGO=r.CODIGO AND c.NUMERO=r.NUMERO WHERE r.NOTA IS NULL ORDER BY a.DATA,u.NOME");}
    public List<Map<String,Object>> relatorioMedias(){return jdbc.queryForList("SELECT a.CODIGO,a.TITULO,ROUND(AVG(nt.NOTA_TOTAL),2) AS MEDIA,COUNT(DISTINCT f.REGISTRO) AS PARTICIPANTES FROM Avaliacao a LEFT JOIN Faz f ON f.CODIGO=a.CODIGO LEFT JOIN Nota_Total nt ON nt.CODIGO=a.CODIGO AND nt.REGISTRO=f.REGISTRO GROUP BY a.CODIGO,a.TITULO,a.DATA ORDER BY a.DATA");}
    public List<Map<String,Object>> relatorioAluno(){return jdbc.queryForList("SELECT nt.REGISTRO,nt.NOME,nt.CODIGO,nt.TITULO,nt.NOTA_TOTAL,ROUND((SELECT AVG(nt2.NOTA_TOTAL) FROM Nota_Total nt2 WHERE nt2.CODIGO=nt.CODIGO),2) AS MEDIA_TURMA FROM Nota_Total nt ORDER BY nt.NOME,nt.CODIGO");}
    public List<Map<String,Object>> relatorioQuestoes(){return jdbc.queryForList("SELECT q.NUMERO,q.ENUNCIADO,COUNT(r.REGISTRO) AS RESPOSTAS,COUNT(r.REGISTRO) FILTER (WHERE r.RESPOSTA<>o.CORRETA) AS ERROS,ROUND(100.0*COUNT(r.REGISTRO) FILTER (WHERE r.RESPOSTA<>o.CORRETA)/NULLIF(COUNT(r.REGISTRO),0),2) AS PERCENTUAL_ERRO FROM Questao q JOIN Objetiva o ON o.NUMERO=q.NUMERO LEFT JOIN Contem c ON c.NUMERO=q.NUMERO LEFT JOIN Resposta r ON r.CODIGO=c.CODIGO AND r.NUMERO=c.NUMERO GROUP BY q.NUMERO,q.ENUNCIADO HAVING COUNT(r.REGISTRO)>0 ORDER BY PERCENTUAL_ERRO DESC NULLS LAST,q.NUMERO");}
}
