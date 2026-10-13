package br.com.athletiza.model;

/**
 * Módulos do sistema, na ordem do menu lateral.
 */
public enum Modulo {

    INICIO("Início"),
    GESTAO("Gestão"),
    ATLETAS("Atletas"),
    COMPETICOES("Competições"),
    TREINOS("Treinos"),
    EVENTOS("Eventos"),
    DEMANDAS("Demandas"),
    REGISTROS("Registros"),
    USUARIOS("Usuários");

    private final String nome;

    Modulo(String nome) {
        this.nome = nome;
    }

    /** Nome exibido no menu. */
    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
