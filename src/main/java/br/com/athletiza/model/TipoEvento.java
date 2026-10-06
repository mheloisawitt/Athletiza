package br.com.athletiza.model;

/**
 * Tipos de evento organizados pela atlética (RF17).
 */
public enum TipoEvento {

    FESTA("Festa"),
    ACAO_SOCIAL("Social"),
    ACAO_AMBIENTAL("Ambiental"),
    COMPETICAO("Competição"),
    OUTRO("Outro");

    private final String descricao;

    TipoEvento(String descricao) {
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
