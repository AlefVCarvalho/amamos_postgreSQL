// Padroniza as mensagens de erro enviadas pela API ao frontend.
package br.uel.avaliacoes.controller;
import java.util.Map; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,String>> tratarRegra(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("erro",e.getMessage()));}
    @ExceptionHandler(Exception.class) public ResponseEntity<Map<String,String>> tratarErro(Exception e){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro","Não foi possível concluir a operação. Verifique os dados e o banco de dados."));}
}
