package br.com.athletiza.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * Pasta da galeria de registros (ex.: Festas, Ações Sociais). Ordem natural: pelo nome.
 */
public class Pasta extends Entidade implements Comparable<Pasta> {

    private static final Comparator<Pasta> POR_NOME = Comparator.comparing(Pasta::getNome, Textos.COMPARADOR);

    private String nome;
    private int quantidadeArquivos;

    public Pasta() {
    }

    public Pasta(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    /** Quantidade de arquivos, preenchida pelo DAO na listagem. */
    public int getQuantidadeArquivos() {
        return quantidadeArquivos;
    }

    public void setQuantidadeArquivos(int quantidadeArquivos) {
        this.quantidadeArquivos = quantidadeArquivos;
    }

    @Override
    public int compareTo(Pasta outra) {
        return POR_NOME.compare(this, outra);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pasta outra) || nome == null) {
            return false;
        }
        return nome.equalsIgnoreCase(outra.nome);
    }

    @Override
    public int hashCode() {
        return nome == null ? System.identityHashCode(this) : Objects.hash(nome.toLowerCase());
    }

    @Override
    public String toString() {
        return nome;
    }
}
