// Endpoints de cadastro e consulta de questões.
package br.uel.avaliacoes.controller;
import java.util.*; import jakarta.servlet.http.HttpSession; import org.springframework.http.HttpStatus; import org.springframework.web.bind.annotation.*; import br.uel.avaliacoes.model.Questao; import br.uel.avaliacoes.service.QuestaoService;
@RestController @RequestMapping("/api/questoes")
public class QuestaoController {
    private final QuestaoService service; public QuestaoController(QuestaoService s){service=s;}
    @GetMapping public List<Map<String,Object>> listar(){return service.listar();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> cadastrar(@RequestBody Questao q,HttpSession session){if(!"PROFESSOR".equals(session.getAttribute("papel"))) throw new IllegalArgumentException("Entre como professor para cadastrar questões.");return Map.of("numero",service.cadastrar(q),"mensagem","Questão cadastrada com sucesso.");}
}
