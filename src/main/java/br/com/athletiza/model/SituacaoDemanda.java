package br.com.athletiza.model;

/**
 * Situação de uma demanda da atlética.
 */
public enum SituacaoDemanda {

    ABERTA("Aberta"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    SituacaoDemanda(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Demanda que ainda precisa de alguém trabalhando nela. */
    public boolean isPendente() {
        return this == ABERTA || this == EM_ANDAMENTO;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
