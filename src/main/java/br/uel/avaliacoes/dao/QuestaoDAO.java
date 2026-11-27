// Operações JDBC de cadastro e consulta de questões.
package br.uel.avaliacoes.dao;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import br.uel.avaliacoes.model.Questao;
@Repository
public class QuestaoDAO {
    private final JdbcTemplate jdbc;
    public QuestaoDAO(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public List<Map<String,Object>> listar(){
        return jdbc.queryForList("SELECT q.NUMERO,q.ENUNCIADO,CASE WHEN o.NUMERO IS NOT NULL THEN 'OBJETIVA' ELSE 'SUBJETIVA' END AS TIPO FROM Questao q LEFT JOIN Objetiva o ON o.NUMERO=q.NUMERO ORDER BY q.NUMERO");
    }
    public int proximoNumero(){Integer n=jdbc.queryForObject("SELECT COALESCE(MAX(NUMERO),0)+1 FROM Questao",Integer.class);return n==null?1:n;}
    public void inserirBase(Questao q){jdbc.update("INSERT INTO Questao(NUMERO,ENUNCIADO) VALUES (?,?)",q.getNumero(),q.getEnunciado());}
    public void inserirObjetiva(Questao q){jdbc.update("INSERT INTO Objetiva(NUMERO,CORRETA) VALUES (?,?)",q.getNumero(),q.getCorreta()); for(String alternativa:q.getAlternativas()){
            String codigo=alternativa;
            String descricao="";
            int separador=alternativa.indexOf(") ");
            if(separador<0) separador=alternativa.indexOf(" - ");
            if(separador>0){
                codigo=alternativa.substring(0,separador).trim();
                descricao=alternativa.substring(separador+ (alternativa.startsWith(" - ",separador)?3:2)).trim();
            }
            jdbc.update("INSERT INTO Alternativas(NUMERO,ALTERNATIVA,DESCRICAO) VALUES (?,?,?)",q.getNumero(),codigo,descricao);
        }}
    public void inserirSubjetiva(Questao q){jdbc.update("INSERT INTO Subjetiva(NUMERO,EXPECTATIVA) VALUES (?,?)",q.getNumero(),q.getExpectativa());}
    public List<Map<String,Object>> listarPorAvaliacao(int codigo){
        List<Map<String,Object>> questoes=jdbc.queryForList("SELECT q.NUMERO,q.ENUNCIADO,c.VALOR,CASE WHEN o.NUMERO IS NOT NULL THEN 'OBJETIVA' ELSE 'SUBJETIVA' END AS TIPO FROM Contem c JOIN Questao q ON q.NUMERO=c.NUMERO LEFT JOIN Objetiva o ON o.NUMERO=q.NUMERO WHERE c.CODIGO=? ORDER BY q.NUMERO",codigo);
        for(Map<String,Object> questao:questoes){
            if("OBJETIVA".equals(questao.get("tipo"))){
                List<Map<String,Object>> alternativas=jdbc.queryForList("SELECT ALTERNATIVA AS CODIGO,COALESCE(NULLIF(DESCRICAO,''),ALTERNATIVA) AS TEXTO FROM Alternativas WHERE NUMERO=? ORDER BY ALTERNATIVA",questao.get("numero"));
                questao.put("alternativas",alternativas);
            }
        }
        return questoes;
    }
}
