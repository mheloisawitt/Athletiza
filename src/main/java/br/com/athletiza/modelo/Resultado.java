package br.com.athletiza.modelo;

public class Resultado extends Entidade {
    private Modalidade modalidade;
    private Atleta atleta;
    private Integer colocacao;
    private String placar;
    private String observacao;

    public Resultado() {
    }

    public Resultado(Modalidade modalidade, Atleta atleta, Integer colocacao, String placar) {
        this.modalidade = modalidade;
        this.atleta = atleta;
        this.colocacao = colocacao;
        this.placar = placar;
    }

    public boolean isResultadoDeEquipe() {
        return atleta == null;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public Atleta getAtleta() {
        return atleta;
    }

    public void setAtleta(Atleta atleta) {
        this.atleta = atleta;
    }

    public Integer getColocacao() {
        return colocacao;
    }

    public void setColocacao(Integer colocacao) {
        this.colocacao = colocacao;
    }

    public String getPlacar() {
        return placar;
    }

    public void setPlacar(String placar) {
        this.placar = placar;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
