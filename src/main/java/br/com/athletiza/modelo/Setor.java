package br.com.athletiza.modelo;

public enum Setor {
    PRESIDENCIA("Presidência"),
    ESPORTES("Esportes"),
    EVENTOS("Eventos"),
    SOCIAL("Ações sociais"),
    AMBIENTAL("Ações ambientais"),
    FINANCEIRO("Financeiro"),
    SECRETARIA("Secretaria");

    private final String descricao;

    Setor(String descricao) {
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
