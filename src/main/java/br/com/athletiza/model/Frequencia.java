package br.com.athletiza.model;

/**
 * Frequência de um atleta nos treinos de um período (CH-37).
 * Ordem natural: maior frequência primeiro e, empatando, pelo nome.
 */
public class Frequencia implements Comparable<Frequencia> {

    private final Atleta atleta;
    private final int treinos;
    private final int presencas;

    public Frequencia(Atleta atleta, int treinos, int presencas) {
        this.atleta = atleta;
        this.treinos = treinos;
        this.presencas = presencas;
    }

    public Atleta getAtleta() {
        return atleta;
    }

    public int getTreinos() {
        return treinos;
    }

    public int getPresencas() {
        return presencas;
    }

    /** Percentual de presença, de 0 a 100. */
    public int getPercentual() {
        return treinos == 0 ? 0 : Math.round(presencas * 100f / treinos);
    }

    public int getFaltas() {
        return treinos - presencas;
    }

    @Override
    public int compareTo(Frequencia outra) {
        if (getPercentual() != outra.getPercentual()) {
            return Integer.compare(outra.getPercentual(), getPercentual()); // invertido: maior primeiro
        }
        return atleta.compareTo(outra.atleta);
    }
}
