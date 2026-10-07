package br.com.athletiza.model;

/**
 * Aviso exibido na tela inicial (RF24): tarefa atrasada, treino sem lista de
 * presença ou competição se aproximando.
 */
public record Alerta(Tipo tipo, String texto) implements Comparable<Alerta> {

    /** Tipos de alerta, do mais urgente para o menos urgente. */
    public enum Tipo {
        TAREFA_ATRASADA, PRESENCA_PENDENTE, COMPETICAO_PROXIMA
    }

    @Override
    public int compareTo(Alerta outro) {
        return tipo.compareTo(outro.tipo);
    }
}
