package br.com.athletiza.model;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resultado obtido em uma competição, por modalidade (RF16).
 * Quando o atleta é nulo, o resultado é da equipe.
 */
public class Resultado extends Entidade {

    /** Placar no formato "3 x 1" (gols da atlética primeiro). Também aceita "3x1" e "3 - 1". */
    private static final Pattern PLACAR = Pattern.compile("\\s*(\\d+)\\s*[xX-]\\s*(\\d+)\\s*");

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

    /**
     * Vitória, empate ou derrota, comparando os dois números do placar.
     * Vazio quando não há placar ou ele não está no formato "3 x 1".
     */
    public Optional<Desfecho> getDesfecho() {
        if (placar == null) {
            return Optional.empty();
        }
        Matcher numeros = PLACAR.matcher(placar);
        if (!numeros.matches()) {
            return Optional.empty();
        }
        int nossos = Integer.parseInt(numeros.group(1));
        int deles = Integer.parseInt(numeros.group(2));
        if (nossos > deles) {
            return Optional.of(Desfecho.VITORIA);
        }
        return Optional.of(nossos == deles ? Desfecho.EMPATE : Desfecho.DERROTA);
    }

    /** Colocação de 1 a 3 (ouro, prata ou bronze). */
    public boolean isMedalha() {
        return colocacao != null && colocacao >= 1 && colocacao <= 3;
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
