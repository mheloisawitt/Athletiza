package br.com.athletiza.modelo;

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
