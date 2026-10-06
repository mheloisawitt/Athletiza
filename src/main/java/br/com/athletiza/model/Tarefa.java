package br.com.athletiza.model;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * Tarefa ligada a um evento ou competição, com responsável e prazo (RF19).
 * Ordem natural: pelo prazo.
 */
public class Tarefa extends Entidade implements Comparable<Tarefa> {

    private static final Comparator<Tarefa> POR_PRAZO = Comparator.comparing(Tarefa::getPrazo,
            Comparator.nullsLast(Comparator.naturalOrder()));

    private String descricao;
    private Membro responsavel;
    private LocalDate prazo;
    private SituacaoTarefa situacao = SituacaoTarefa.PENDENTE;

    public Tarefa() {
    }

    public Tarefa(String descricao, Membro responsavel, LocalDate prazo) {
        this.descricao = descricao;
        this.responsavel = responsavel;
        this.prazo = prazo;
    }

    /** Tarefa não concluída com prazo anterior à data informada (CH-41). */
    public boolean isAtrasada(LocalDate hoje) {
        return situacao != SituacaoTarefa.CONCLUIDA && prazo != null && prazo.isBefore(hoje);
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Membro getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Membro responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    public SituacaoTarefa getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoTarefa situacao) {
        this.situacao = situacao;
    }

    @Override
    public int compareTo(Tarefa outra) {
        return POR_PRAZO.compare(this, outra);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
