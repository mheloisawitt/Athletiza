package br.com.athletiza.model;

/**
 * Resumo da lista de presença de um treino: quantos estavam presentes
 * e quantos atletas tiveram a presença registrada (CH-34).
 */
public class ResumoPresenca {

    private final int presentes;
    private final int registrados;

    public ResumoPresenca(int presentes, int registrados) {
        this.presentes = presentes;
        this.registrados = registrados;
    }

    public int getPresentes() {
        return presentes;
    }

    public int getRegistrados() {
        return registrados;
    }

    /** Texto para a tabela de treinos, ex.: "8 de 10". */
    @Override
    public String toString() {
        return presentes + " de " + registrados;
    }
}
