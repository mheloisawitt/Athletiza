package br.com.athletiza.modelo;

public record Alerta(Tipo tipo, String texto) implements Comparable<Alerta> {
    public enum Tipo {
        TAREFA_ATRASADA, PRESENCA_PENDENTE, COMPETICAO_PROXIMA
    }

    @Override
    public int compareTo(Alerta outro) {
        return tipo.compareTo(outro.tipo);
    }
}
