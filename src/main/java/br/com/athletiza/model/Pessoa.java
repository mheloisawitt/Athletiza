package br.com.athletiza.model;

import java.util.Objects;

public abstract class Pessoa extends Entidade {
    private String nome;
    private String matricula;
    private String contato;

    protected Pessoa() {
    }

    protected Pessoa(String nome, String matricula, String contato) {
        this.nome = nome;
        this.matricula = matricula;
        this.contato = contato;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass() || matricula == null) {
            return false;
        }
        return matricula.equals(((Pessoa) o).matricula);
    }

    @Override
    public int hashCode() {
        return matricula == null ? System.identityHashCode(this) : Objects.hash(getClass(), matricula);
    }

    @Override
    public String toString() {
        return nome;
    }
}
