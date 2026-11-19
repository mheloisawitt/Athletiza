package br.com.athletiza.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.Comparator;
import java.util.Objects;

@Entity
@Table(name = "modalidade")
public class Modalidade extends Entidade implements Comparable<Modalidade> {
    private static final Comparator<Modalidade> ORDEM_NATURAL = Comparator
            .comparing(Modalidade::getNome, Textos.COMPARADOR)
            .thenComparing(Modalidade::getGenero);

    @Column(nullable = false, length = 50)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Genero genero;

    public Modalidade() {
    }

    public Modalidade(String nome, Genero genero) {
        this.nome = nome;
        this.genero = genero;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    @Override
    public int compareTo(Modalidade outra) {
        return ORDEM_NATURAL.compare(this, outra);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Modalidade outra) || nome == null) {
            return false;
        }
        return nome.equalsIgnoreCase(outra.nome) && genero == outra.genero;
    }

    @Override
    public int hashCode() {
        return nome == null ? System.identityHashCode(this) : Objects.hash(nome.toLowerCase(), genero);
    }

    @Override
    public String toString() {
        return genero == null ? nome : nome + " (" + genero + ")";
    }
}
