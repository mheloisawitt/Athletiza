package br.com.athletiza.model;

public enum Modulo {
    INICIO("Início"),
    DASHBOARD("Dashboard"),
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

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
