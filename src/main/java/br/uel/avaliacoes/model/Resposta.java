// Representa a resposta enviada por um aluno para uma questão.
package br.uel.avaliacoes.model;
import java.math.BigDecimal;
public class Resposta {
    private Integer registro; private Integer codigo; private Integer numero; private String resposta; private BigDecimal nota;
    public Integer getRegistro(){return registro;} public void setRegistro(Integer v){registro=v;}
    public Integer getCodigo(){return codigo;} public void setCodigo(Integer v){codigo=v;}
    public Integer getNumero(){return numero;} public void setNumero(Integer v){numero=v;}
    public String getResposta(){return resposta;} public void setResposta(String v){resposta=v;}
    public BigDecimal getNota(){return nota;} public void setNota(BigDecimal v){nota=v;}
}
