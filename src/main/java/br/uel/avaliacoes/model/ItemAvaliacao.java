// Representa uma questão incluída em uma avaliação e seu valor em pontos.
package br.uel.avaliacoes.model;
import java.math.BigDecimal;
public class ItemAvaliacao {
    private Integer numero; private BigDecimal valor;
    public Integer getNumero(){return numero;} public void setNumero(Integer v){numero=v;}
    public BigDecimal getValor(){return valor;} public void setValor(BigDecimal v){valor=v;}
}
