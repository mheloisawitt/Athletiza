package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;

/**
 * Resultado obtido em uma competição, por modalidade (RF16).
 * Quando o atleta é nulo, o resultado é da equipe.
 */
public class Resultado extends Entidade {

    /** Regras de preenchimento do lançamento (CH-32). */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(modalidade, "Modalidade")
                .regra(colocacao == null || colocacao > 0, "A \"Colocação\" deve ser maior que zero.")
                .regra(colocacao != null || (placar != null && !placar.isBlank()), "Informe a colocação ou o placar.")
                .tamanhoMaximo(placar, 50, "Placar")
                .tamanhoMaximo(observacao, 500, "Observação")
                .validar();
    }

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
