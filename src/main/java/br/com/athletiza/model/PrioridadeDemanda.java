package br.com.athletiza.model;

/**
 * Prioridade de uma demanda, da mais alta para a mais baixa.
 */
public enum PrioridadeDemanda {

    ALTA("Alta"),
    MEDIA("Média"),
    BAIXA("Baixa");

    private final String descricao;

    PrioridadeDemanda(String descricao) {
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
