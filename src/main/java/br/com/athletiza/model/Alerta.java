package br.com.athletiza.model;

/**
 * Aviso exibido na tela inicial (RF24): tarefa atrasada, treino sem lista de
 * presença ou competição se aproximando.
 * Ordem natural: do tipo mais urgente para o menos urgente.
 */
public class Alerta implements Comparable<Alerta> {

    /** Tipos de alerta, do mais urgente para o menos urgente. */
    public enum Tipo {
        TAREFA_ATRASADA, PRESENCA_PENDENTE, COMPETICAO_PROXIMA
    }

    private final Tipo tipo;
    private final String texto;

    public Alerta(Tipo tipo, String texto) {
        this.tipo = tipo;
        this.texto = texto;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getTexto() {
        return texto;
    }

    @Override
    public int compareTo(Alerta outro) {
        return tipo.compareTo(outro.tipo);
    }

    @Override
    public String toString() {
        return texto;
    }
}
