package br.com.athletiza.model;

/**
 * Gênero da modalidade (ex.: Futsal (M), Futsal (F)).
 */
public enum Genero {

    MASCULINO("M"),
    FEMININO("F"),
    MISTO("Misto");

    private final String descricao;

    Genero(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Nome por extenso, para combos e tabelas. */
    public String getNomeCompleto() {
        return switch (this) {
            case MASCULINO -> "Masculino";
            case FEMININO -> "Feminino";
            case MISTO -> "Misto";
        };
    }

    @Override
    public String toString() {
        return descricao;
    }
}
