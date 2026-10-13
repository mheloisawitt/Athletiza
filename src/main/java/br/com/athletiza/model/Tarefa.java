package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
import java.time.LocalDate;
import java.util.List;

/**
 * Tarefa ligada a um evento ou competição, com responsável e prazo (RF19).
 * Ordem natural: pelo prazo.
 */
public class Tarefa extends Entidade implements Comparable<Tarefa> {

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

    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(descricao, "Descrição")
                .tamanhoMaximo(descricao, 200, "Descrição")
                .obrigatorio(situacao, "Situação")
                .validar();
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

    /** Resumo para as consultas, ex.: "1 de 3 concluídas · 1 atrasada(s)". */
    public static String resumir(List<Tarefa> tarefas, LocalDate hoje) {
        if (tarefas.isEmpty()) {
            return "—";
        }
        int concluidas = 0;
        int atrasadas = 0;
        for (Tarefa tarefa : tarefas) {
            if (tarefa.getSituacao() == SituacaoTarefa.CONCLUIDA) {
                concluidas++;
            }
            if (tarefa.isAtrasada(hoje)) {
                atrasadas++;
            }
        }
        String resumo = concluidas + " de " + tarefas.size() + " concluídas";
        if (atrasadas > 0) {
            resumo += " · " + atrasadas + " atrasada(s)";
        }
        return resumo;
    }

    @Override
    public int compareTo(Tarefa outra) {
        return Textos.compararNulosNoFim(prazo, outra.prazo);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
