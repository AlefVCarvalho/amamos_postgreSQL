// Regras de criação de avaliações e associação de questões e alunos.
package br.uel.avaliacoes.service;
import java.util.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import br.uel.avaliacoes.dao.AvaliacaoDAO; import br.uel.avaliacoes.dao.QuestaoDAO; import br.uel.avaliacoes.model.Avaliacao; import br.uel.avaliacoes.model.ItemAvaliacao;
@Service
public class AvaliacaoService {
    private final AvaliacaoDAO avaliacaoDAO; private final QuestaoDAO questaoDAO;
    public AvaliacaoService(AvaliacaoDAO a,QuestaoDAO q){avaliacaoDAO=a;questaoDAO=q;}
    public List<Map<String,Object>> listarDoAluno(int registro){return avaliacaoDAO.listarDoAluno(registro);}
    public List<Map<String,Object>> listar(){return avaliacaoDAO.listar();}
    @Transactional public int cadastrar(Avaliacao a){
        if(a.getTitulo()==null||a.getTitulo().isBlank()||a.getData()==null||a.getInicio()==null||a.getFim()==null||!a.getFim().isAfter(a.getInicio())) throw new IllegalArgumentException("Preencha os dados da avaliação e informe um horário final posterior ao inicial.");
        if(a.getQuestoes()==null||a.getQuestoes().isEmpty()) throw new IllegalArgumentException("Selecione pelo menos uma questão.");
        if(a.getAlunos()==null||a.getAlunos().isEmpty()) throw new IllegalArgumentException("Selecione pelo menos um aluno.");
        a.setCodigo(avaliacaoDAO.proximoCodigo()); avaliacaoDAO.inserir(a);
        for(ItemAvaliacao item:a.getQuestoes()){
            if(item.getValor()==null||item.getValor().signum()<0) throw new IllegalArgumentException("O valor de cada questão deve ser igual ou maior que zero.");
            avaliacaoDAO.inserirQuestao(a.getCodigo(),item.getNumero(),item.getValor());
        }
        for(Integer aluno:a.getAlunos()) avaliacaoDAO.designarAluno(a.getCodigo(),aluno);
        return a.getCodigo();
    }
    public Map<String,Object> obterParaAluno(int codigo,int registro){return avaliacaoDAO.obterParaAluno(codigo,registro);}
    public boolean alunoDesignado(int codigo,int registro){return avaliacaoDAO.alunoDesignado(codigo,registro);}
    public List<Map<String,Object>> questoes(int codigo){return questaoDAO.listarPorAvaliacao(codigo);}
}
