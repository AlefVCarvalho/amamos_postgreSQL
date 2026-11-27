// Representa uma questão objetiva ou subjetiva e seus dados de apresentação.
package br.uel.avaliacoes.model;
import java.util.List;
public class Questao {
    private Integer numero; private String enunciado; private String tipo; private String correta; private String expectativa; private List<String> alternativas;
    public Integer getNumero(){return numero;} public void setNumero(Integer v){numero=v;}
    public String getEnunciado(){return enunciado;} public void setEnunciado(String v){enunciado=v;}
    public String getTipo(){return tipo;} public void setTipo(String v){tipo=v;}
    public String getCorreta(){return correta;} public void setCorreta(String v){correta=v;}
    public String getExpectativa(){return expectativa;} public void setExpectativa(String v){expectativa=v;}
    public List<String> getAlternativas(){return alternativas;} public void setAlternativas(List<String> v){alternativas=v;}
}
