package br.com.athletiza.model;

/**
 * Desfecho de uma partida para a atlética, calculado a partir do placar (dashboard).
 */
public enum Desfecho {

    VITORIA("Vitórias"),
    EMPATE("Empates"),
    DERROTA("Derrotas");

    private final String descricao;

    Desfecho(String descricao) {
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
