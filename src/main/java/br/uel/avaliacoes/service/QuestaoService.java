// Regras de cadastro de questões e validação de seus tipos.
package br.uel.avaliacoes.service;
import java.util.List; import java.util.Map;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import br.uel.avaliacoes.dao.QuestaoDAO; import br.uel.avaliacoes.model.Questao;
@Service
public class QuestaoService {
    private final QuestaoDAO dao;
    public QuestaoService(QuestaoDAO dao){this.dao=dao;}
    public List<Map<String,Object>> listar(){return dao.listar();}
    @Transactional public int cadastrar(Questao q){
        if(q.getEnunciado()==null||q.getEnunciado().isBlank()) throw new IllegalArgumentException("Informe o enunciado da questão.");
        if(!"OBJETIVA".equals(q.getTipo())&&!"SUBJETIVA".equals(q.getTipo())) throw new IllegalArgumentException("Tipo de questão inválido.");
        if("OBJETIVA".equals(q.getTipo())&&(q.getAlternativas()==null||q.getAlternativas().size()<2||q.getCorreta()==null||q.getAlternativas().stream().noneMatch(a -> a.equals(q.getCorreta()) || a.startsWith(q.getCorreta()+") ") || a.startsWith(q.getCorreta()+" - ")))) throw new IllegalArgumentException("Informe pelo menos duas alternativas e selecione uma delas como correta.");
        if("SUBJETIVA".equals(q.getTipo())&&(q.getExpectativa()==null||q.getExpectativa().isBlank())) throw new IllegalArgumentException("Informe a expectativa de resposta para a questão subjetiva.");
        q.setNumero(dao.proximoNumero()); dao.inserirBase(q);
        if("OBJETIVA".equals(q.getTipo())) dao.inserirObjetiva(q); else dao.inserirSubjetiva(q);
        return q.getNumero();
    }
    public List<Map<String,Object>> listarPorAvaliacao(int codigo){return dao.listarPorAvaliacao(codigo);}
}
