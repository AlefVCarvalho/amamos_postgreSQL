// Endpoints de criação, listagem e realização de avaliações.
package br.uel.avaliacoes.controller;
import java.util.*; import jakarta.servlet.http.HttpSession; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import br.uel.avaliacoes.model.Avaliacao; import br.uel.avaliacoes.service.AvaliacaoService; import br.uel.avaliacoes.service.RespostaService;
@RestController @RequestMapping("/api/avaliacoes")
public class AvaliacaoController {
    private final AvaliacaoService service; private final RespostaService respostaService; public AvaliacaoController(AvaliacaoService s, RespostaService rs){service=s; respostaService=rs;}
    @GetMapping public List<Map<String,Object>> listar(HttpSession session){if("ALUNO".equals(session.getAttribute("papel"))) return service.listarDoAluno(Integer.parseInt(session.getAttribute("registro").toString()));return service.listar();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> cadastrar(@RequestBody Avaliacao a,HttpSession session){if(!"PROFESSOR".equals(session.getAttribute("papel"))) throw new IllegalArgumentException("Entre como professor para cadastrar avaliações.");return Map.of("codigo",service.cadastrar(a),"mensagem","Avaliação cadastrada com sucesso.");}
    @GetMapping("/{codigo}/questoes") public List<Map<String,Object>> questoes(@PathVariable int codigo){return service.questoes(codigo);}
    @GetMapping("/aluno/minhas") public List<Map<String,Object>> minhas(HttpSession session){int registro=alunoLogado(session);return service.listarDoAluno(registro);}
    @GetMapping("/{codigo}/aluno/{registro}") public Map<String,Object> prova(@PathVariable int codigo,@PathVariable int registro,HttpSession session){int aluno=alunoLogado(session);if(aluno!=registro||!service.alunoDesignado(codigo,aluno)) throw new IllegalArgumentException("Este aluno não está autorizado para esta avaliação.");
    if(respostaService.jaRespondeu(aluno,codigo)) throw new IllegalArgumentException("Você já enviou esta avaliação.");Map<String,Object> r=new HashMap<>(service.obterParaAluno(codigo,aluno));r.put("questoes",service.questoes(codigo));return r;}
    private int alunoLogado(HttpSession session){Object registro=session.getAttribute("registro");if(registro==null||!"ALUNO".equals(session.getAttribute("papel"))) throw new IllegalArgumentException("Entre com um registro de aluno para continuar.");return Integer.parseInt(registro.toString());}
}
