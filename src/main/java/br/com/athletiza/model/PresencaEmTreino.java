package br.com.athletiza.model;

/**
 * Linha do histórico de um atleta: o treino e se ele esteve presente (RF13).
 */
public class PresencaEmTreino {

    private final Treino treino;
    private final Presenca presenca;

    public PresencaEmTreino(Treino treino, Presenca presenca) {
        this.treino = treino;
        this.presenca = presenca;
    }

    public Treino getTreino() {
        return treino;
    }

    public Presenca getPresenca() {
        return presenca;
    }
}
