package br.com.athletiza.model;

import java.util.Comparator;

/**
 * Frequência de um atleta nos treinos de um período (CH-37).
 * Ordem natural: maior frequência primeiro e, empatando, pelo nome.
 */
public record Frequencia(Atleta atleta, int treinos, int presencas) implements Comparable<Frequencia> {

    private static final Comparator<Frequencia> ORDEM = Comparator
            .comparingInt(Frequencia::getPercentual).reversed()
            .thenComparing(Frequencia::atleta);

    /** Percentual de presença, de 0 a 100. */
    public int getPercentual() {
        return treinos == 0 ? 0 : Math.round(presencas * 100f / treinos);
    }

    public int getFaltas() {
        return treinos - presencas;
    }

    @Override
    public int compareTo(Frequencia outra) {
        return ORDEM.compare(this, outra);
    }
}
