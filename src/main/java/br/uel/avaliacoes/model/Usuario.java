// Representa os usuários cadastrados no sistema.
package br.uel.avaliacoes.model;
public class Usuario {
    private Integer registro; private String nome; private String papel;
    public Usuario() {}
    public Usuario(Integer registro, String nome, String papel) { this.registro=registro; this.nome=nome; this.papel=papel; }
    public Integer getRegistro(){return registro;} public void setRegistro(Integer v){registro=v;}
    public String getNome(){return nome;} public void setNome(String v){nome=v;}
    public String getPapel(){return papel;} public void setPapel(String v){papel=v;}
}
