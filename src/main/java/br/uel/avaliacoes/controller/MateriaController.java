// Endpoint para consulta das matérias cadastradas.
package br.uel.avaliacoes.controller;
import java.util.List; import org.springframework.web.bind.annotation.*; import br.uel.avaliacoes.dao.MateriaDAO; import br.uel.avaliacoes.model.Materia;
@RestController @RequestMapping("/api/materias")
public class MateriaController { private final MateriaDAO dao; public MateriaController(MateriaDAO d){dao=d;} @GetMapping public List<Materia> listar(){return dao.listar();} }
