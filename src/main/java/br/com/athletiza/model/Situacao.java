package br.com.athletiza.model;

public enum Situacao {
    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String descricao;

    Situacao(String descricao) {
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
