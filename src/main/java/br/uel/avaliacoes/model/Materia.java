// Representa uma matéria vinculada a usuários e avaliações.
package br.uel.avaliacoes.model;
public class Materia {
    private String sigla; private String nome;
    public Materia(){} public Materia(String sigla,String nome){this.sigla=sigla;this.nome=nome;}
    public String getSigla(){return sigla;} public void setSigla(String v){sigla=v;}
    public String getNome(){return nome;} public void setNome(String v){nome=v;}
}
