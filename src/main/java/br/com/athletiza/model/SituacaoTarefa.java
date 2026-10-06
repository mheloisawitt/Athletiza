package br.com.athletiza.model;

/**
 * Situação de uma tarefa (RF19).
 */
public enum SituacaoTarefa {

    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída");

    private final String descricao;

    SituacaoTarefa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
