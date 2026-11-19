package br.com.athletiza.modelo;

public enum Perfil {
    ADMINISTRADOR("Administrador"),

    PRESIDENCIA("Presidência"),

    DIRETOR("Diretor"),

    ASSESSOR("Assessor");

    private final String descricao;

    Perfil(String descricao) {
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
