package br.com.athletiza.model;

/**
 * Situação de uma gestão da atlética.
 */
public enum SituacaoGestao {

    PLANEJADA("Planejada"),
    EM_ANDAMENTO("Em andamento"),
    ENCERRADA("Encerrada");

    private final String descricao;

    SituacaoGestao(String descricao) {
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
