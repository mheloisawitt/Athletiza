package br.com.athletiza.model;

public enum TipoEvento {
    FESTA("Festa", Setor.EVENTOS),
    ACAO_SOCIAL("Social", Setor.SOCIAL),
    ACAO_AMBIENTAL("Ambiental", Setor.AMBIENTAL),
    COMPETICAO("Competição", Setor.ESPORTES),
    OUTRO("Outro", Setor.EVENTOS);

    private final String descricao;
    private final Setor setor;

    TipoEvento(String descricao, Setor setor) {
        this.descricao = descricao;
        this.setor = setor;
    }

    public String getDescricao() {
        return descricao;
    }

    public Setor getSetor() {
        return setor;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
