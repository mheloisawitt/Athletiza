package br.com.athletiza.model;

/**
 * Setor da gestão a que um cargo pertence. Define o que o ocupante do cargo
 * pode fazer no sistema (ver Acesso).
 */
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
