package br.com.athletiza.model;

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
