// Representa uma avaliação, suas questões e os alunos designados.
package br.uel.avaliacoes.model;
import java.time.LocalDate; import java.time.LocalTime; import java.util.List;
public class Avaliacao {
    private Integer codigo; private String titulo; private String descricao; private LocalTime inicio; private LocalTime fim; private LocalDate data; private Integer registroProfessor; private String siglaMateria; private List<ItemAvaliacao> questoes; private List<Integer> alunos;
    public Integer getCodigo(){return codigo;} public void setCodigo(Integer v){codigo=v;}
    public String getTitulo(){return titulo;} public void setTitulo(String v){titulo=v;}
    public String getDescricao(){return descricao;} public void setDescricao(String v){descricao=v;}
    public LocalTime getInicio(){return inicio;} public void setInicio(LocalTime v){inicio=v;}
    public LocalTime getFim(){return fim;} public void setFim(LocalTime v){fim=v;}
    public LocalDate getData(){return data;} public void setData(LocalDate v){data=v;}
    public Integer getRegistroProfessor(){return registroProfessor;} public void setRegistroProfessor(Integer v){registroProfessor=v;}
    public String getSiglaMateria(){return siglaMateria;} public void setSiglaMateria(String v){siglaMateria=v;}
    public List<ItemAvaliacao> getQuestoes(){return questoes;} public void setQuestoes(List<ItemAvaliacao> v){questoes=v;}
    public List<Integer> getAlunos(){return alunos;} public void setAlunos(List<Integer> v){alunos=v;}
}
