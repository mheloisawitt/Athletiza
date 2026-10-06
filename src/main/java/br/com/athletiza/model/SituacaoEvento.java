package br.com.athletiza.model;

/**
 * Situação de um evento organizado pela atlética.
 */
public enum SituacaoEvento {

    PLANEJADO("Planejado"),
    CONFIRMADO("Confirmado"),
    REALIZADO("Realizado"),
    CANCELADO("Cancelado");

    private final String descricao;

    SituacaoEvento(String descricao) {
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
