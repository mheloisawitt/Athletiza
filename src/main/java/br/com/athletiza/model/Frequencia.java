package br.com.athletiza.model;

import java.util.Comparator;

public record Frequencia(Atleta atleta, int treinos, int presencas) implements Comparable<Frequencia> {
    private static final Comparator<Frequencia> ORDEM = Comparator
            .comparingInt(Frequencia::getPercentual).reversed()
            .thenComparing(Frequencia::atleta);

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
