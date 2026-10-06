package br.com.athletiza.model;

/**
 * Tipos de atividade exibidos no calendário. Cada tipo tem uma cor
 * para diferenciar as atividades visualmente (CH-15).
 */
public enum TipoAtividade {

    TREINO("Treino", "#8A2BE2"),
    AMISTOSO("Amistoso", "#B266FF"),
    COMPETICAO("Competição", "#00FF6A"),
    EVENTO("Evento", "#FF4FD8"),
    COMPROMISSO("Compromisso", "#3D9BFF");

    private final String descricao;
    private final String corHex;

    TipoAtividade(String descricao, String corHex) {
        this.descricao = descricao;
        this.corHex = corHex;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCorHex() {
        return corHex;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
