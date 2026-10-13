package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
import java.util.Objects;

/**
 * Pasta da galeria de registros (ex.: Festas, Ações Sociais). Ordem natural: pelo nome.
 */
public class Pasta extends Entidade implements Comparable<Pasta> {

    private String nome;
    private int quantidadeArquivos;

    public Pasta() {
    }

    public Pasta(String nome) {
        this.nome = nome;
    }

    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(nome, "Nome da pasta")
                .tamanhoMaximo(nome, 60, "Nome da pasta")
                .validar();
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
        return Textos.comparar(nome, outra.nome);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pasta) || nome == null) {
            return false;
        }
        Pasta outra = (Pasta) o;
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
