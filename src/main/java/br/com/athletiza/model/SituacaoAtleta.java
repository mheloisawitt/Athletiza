package br.com.athletiza.model;

/**
 * Situação do atleta em uma modalidade específica (RF04).
 */
public enum SituacaoAtleta {

    ATIVO("Ativo"),
    LESIONADO("Lesionado"),
    AFASTADO("Afastado"),
    INATIVO("Inativo");

    private final String descricao;

    SituacaoAtleta(String descricao) {
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
