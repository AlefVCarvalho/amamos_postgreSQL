// Endpoints para envio de respostas, correção de questões subjetivas e relatórios.
package br.uel.avaliacoes.controller;
import java.math.BigDecimal; import jakarta.servlet.http.HttpSession; import java.util.*; import org.springframework.web.bind.annotation.*; import br.uel.avaliacoes.service.RespostaService;
@RestController @RequestMapping("/api")
public class RespostaController {
    private final RespostaService service; public RespostaController(RespostaService s){service=s;}
    @PostMapping("/avaliacoes/{codigo}/respostas/{registro}") public Map<String,String> enviar(@PathVariable int codigo,@PathVariable int registro,@RequestBody Map<String,List<Map<String,Object>>> body,HttpSession session){int aluno=alunoLogado(session);if(aluno!=registro) throw new IllegalArgumentException("Não é permitido enviar respostas em nome de outro aluno.");service.enviar(aluno,codigo,body.get("respostas"));return Map.of("mensagem","Respostas registradas. As questões objetivas foram corrigidas automaticamente.");}
    private void professorLogado(HttpSession session){if(!"PROFESSOR".equals(session.getAttribute("papel"))) throw new IllegalArgumentException("Entre com um registro de professor para acessar esta área.");}
    private int alunoLogado(HttpSession session){Object registro=session.getAttribute("registro");if(registro==null||!"ALUNO".equals(session.getAttribute("papel"))) throw new IllegalArgumentException("Entre com um registro de aluno para continuar.");return Integer.parseInt(registro.toString());}
    @GetMapping("/correcoes/pendentes") public List<Map<String,Object>> pendentes(HttpSession session){professorLogado(session);return service.pendentes();}
    @PutMapping("/correcoes") public Map<String,String> corrigir(@RequestBody Map<String,Object> body,HttpSession session){professorLogado(session);service.corrigir(Integer.parseInt(body.get("registro").toString()),Integer.parseInt(body.get("codigo").toString()),Integer.parseInt(body.get("numero").toString()),new BigDecimal(body.get("nota").toString()));return Map.of("mensagem","Nota da questão subjetiva atualizada.");}
    @GetMapping("/relatorios/medias") public List<Map<String,Object>> medias(HttpSession session){professorLogado(session);return service.medias();}
    @GetMapping("/relatorios/alunos") public List<Map<String,Object>> alunos(HttpSession session){professorLogado(session);return service.alunos();}
    @GetMapping("/relatorios/questoes") public List<Map<String,Object>> questoes(HttpSession session){professorLogado(session);return service.questoes();}
}
