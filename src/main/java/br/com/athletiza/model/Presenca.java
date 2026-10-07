package br.com.athletiza.model;

/**
 * Presença de um atleta em um treino.
 */
public enum Presenca {

    PRESENTE("Presente"),
    AUSENTE("Ausente");

    private final String descricao;

    Presenca(String descricao) {
        this.descricao = descricao;
    }

    public static Presenca de(boolean presente) {
        return presente ? PRESENTE : AUSENTE;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
