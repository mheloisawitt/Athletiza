package br.com.athletiza.modelo;

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
