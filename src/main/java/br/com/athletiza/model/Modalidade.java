package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
import java.util.Objects;

/**
 * Modalidade esportiva da atlética, por exemplo "Futsal (F)" (RF05).
 * Duas modalidades são iguais quando têm o mesmo nome e gênero.
 */
public class Modalidade extends Entidade implements Comparable<Modalidade> {

    private String nome;
    private Genero genero;

    public Modalidade() {
    }

    public Modalidade(String nome, Genero genero) {
        this.nome = nome;
        this.genero = genero;
    }

    /** Regras de preenchimento do cadastro (CH-27). */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(nome, "Nome")
                .tamanhoMaximo(nome, 50, "Nome")
                .obrigatorio(genero, "Gênero")
                .validar();
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

    /** Ordem natural: pelo nome e, empatando, pelo gênero. */
    @Override
    public int compareTo(Modalidade outra) {
        int resultado = Textos.comparar(nome, outra.nome);
        if (resultado != 0) {
            return resultado;
        }
        return Textos.compararNulosNoFim(genero, outra.genero);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Modalidade) || nome == null) {
            return false;
        }
        Modalidade outra = (Modalidade) o;
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
