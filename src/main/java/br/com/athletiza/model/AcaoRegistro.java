package br.com.athletiza.model;

/**
 * Ação registrada no histórico de operações.
 */
public enum AcaoRegistro {

    INCLUSAO("Inclusão"),
    ALTERACAO("Alteração"),
    EXCLUSAO("Exclusão"),
    LOGIN("Login");

    private final String descricao;

    AcaoRegistro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
