// Regras para envio de respostas, correção manual e relatórios.
package br.uel.avaliacoes.service;
import java.math.BigDecimal; import java.util.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import br.uel.avaliacoes.dao.RespostaDAO;
@Service
public class RespostaService {
    private final RespostaDAO dao;
    public RespostaService(RespostaDAO dao){this.dao=dao;}
    @Transactional public void enviar(int aluno,int codigo,List<Map<String,Object>> respostas){
        if(!dao.alunoDesignado(aluno,codigo)) throw new IllegalArgumentException("Este aluno não está designado para a avaliação.");
        dao.bloquearDesignacao(aluno,codigo);
        if(dao.jaRespondeu(aluno,codigo)) throw new IllegalArgumentException("Você já enviou esta avaliação. Não é permitido responder novamente.");
        if(respostas==null||respostas.isEmpty()) throw new IllegalArgumentException("A avaliação não contém respostas.");
        if(dao.quantidadeQuestoes(codigo)==0) throw new IllegalArgumentException("A avaliação não possui questões.");
        Set<Integer> recebidas=new HashSet<>();
        for(Map<String,Object> r:respostas){Object numero=r.get("numero"),texto=r.get("resposta");if(numero==null||texto==null||texto.toString().isBlank()) throw new IllegalArgumentException("Responda todas as questões antes de enviar.");int n=Integer.parseInt(numero.toString());if(!recebidas.add(n)||!dao.questaoDaAvaliacao(codigo,n)) throw new IllegalArgumentException("A resposta contém uma questão inválida ou repetida.");dao.salvar(aluno,codigo,n,texto.toString());}
        if(recebidas.size()!=dao.quantidadeQuestoes(codigo)) throw new IllegalArgumentException("Envie uma resposta para cada questão da avaliação.");
        dao.marcarRespondida(aluno,codigo);
    }
    public boolean jaRespondeu(int aluno,int codigo){return dao.jaRespondeu(aluno,codigo);}
    public void corrigir(int aluno,int codigo,int numero,BigDecimal nota){if(nota==null||nota.signum()<0) throw new IllegalArgumentException("A nota deve ser igual ou maior que zero.");dao.corrigir(aluno,codigo,numero,nota);}
    public List<Map<String,Object>> pendentes(){return dao.pendentes();}
    public List<Map<String,Object>> medias(){return dao.relatorioMedias();}
    public List<Map<String,Object>> alunos(){return dao.relatorioAluno();}
    public List<Map<String,Object>> questoes(){return dao.relatorioQuestoes();}
}
