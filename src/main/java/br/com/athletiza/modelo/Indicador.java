package br.com.athletiza.modelo;

import java.util.Comparator;

public record Indicador(String nome, int valor, int parcial, String detalhe) implements Comparable<Indicador> {
    private static final Comparator<Indicador> ORDEM = Comparator
            .comparingInt(Indicador::valor).reversed()
            .thenComparing(Indicador::parcial, Comparator.reverseOrder())
            .thenComparing(Indicador::nome, Textos.COMPARADOR);

    public Indicador(String nome, int valor, String detalhe) {
        this(nome, valor, 0, detalhe);
    }

    @Override
    public int compareTo(Indicador outro) {
        return ORDEM.compare(this, outro);
    }
}
