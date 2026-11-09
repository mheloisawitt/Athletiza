package br.com.athletiza.model;

import java.util.Comparator;

/**
 * Uma linha de um gráfico do dashboard: um nome (modalidade, atleta ou setor)
 * e o valor medido para ele.
 *
 * @param nome    o que está sendo medido (ex.: "Futsal (F)", "Diretor de Eventos")
 * @param valor   valor total da barra
 * @param parcial parte do valor exibida com outra cor (ex.: amistosos dentro do total
 *                de treinos, demandas atrasadas dentro das abertas); 0 quando não se aplica
 * @param detalhe texto complementar exibido ao passar o mouse
 *
 * Ordem natural: maior valor primeiro e, empatando, pelo nome.
 */
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
