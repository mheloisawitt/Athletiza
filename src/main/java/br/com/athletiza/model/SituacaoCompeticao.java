package br.com.athletiza.model;

/**
 * Situação da participação da atlética em uma competição.
 */
public enum SituacaoCompeticao {

    PLANEJADA("Planejada"),
    CONFIRMADA("Confirmada"),
    ENCERRADA("Encerrada"),
    CANCELADA("Cancelada");

    private final String descricao;

    SituacaoCompeticao(String descricao) {
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
