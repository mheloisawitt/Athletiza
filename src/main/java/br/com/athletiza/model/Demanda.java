package br.com.athletiza.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Demanda extends Entidade implements Comparable<Demanda> {
    private static final Comparator<Demanda> ORDEM = Comparator
            .comparing((Demanda d) -> d.situacao == null || !d.situacao.isPendente())
            .thenComparing(Demanda::getPrioridade, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Demanda::getPrazo, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Demanda::getTitulo, Textos.COMPARADOR);

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String titulo;
    private String descricao;
    private PrioridadeDemanda prioridade = PrioridadeDemanda.MEDIA;
    private SituacaoDemanda situacao = SituacaoDemanda.ABERTA;
    private LocalDate prazo;

    private Atividade relacionada;
    private final Set<Membro> responsaveis = new LinkedHashSet<>();

    private Usuario solicitante;

    public Demanda() {
    }

    public Demanda(String titulo, PrioridadeDemanda prioridade, LocalDate prazo) {
        this.titulo = titulo;
        this.prioridade = prioridade;
        this.prazo = prazo;
    }

    public boolean isAtrasada(LocalDate hoje) {
        return situacao != null && situacao.isPendente() && prazo != null && prazo.isBefore(hoje);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public PrioridadeDemanda getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeDemanda prioridade) {
        this.prioridade = prioridade;
    }

    public SituacaoDemanda getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoDemanda situacao) {
        this.situacao = situacao;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public String getNomeSolicitante() {
        return solicitante == null || solicitante.getNome() == null ? "" : solicitante.getNome();
    }

    public boolean isResponsavel(Membro membro) {
        return membro != null && responsaveis.contains(membro);
    }

    public Atividade getRelacionada() {
        return relacionada;
    }

    public void setRelacionada(Atividade relacionada) {
        if (relacionada != null && !(relacionada instanceof Evento) && !(relacionada instanceof Competicao)) {
            throw new IllegalArgumentException("Uma demanda só pode ser relacionada a um evento ou a uma competição.");
        }
        this.relacionada = relacionada;
    }

    public String getDescricaoRelacionada() {
        return descreverRelacionada(relacionada);
    }

    public static String descreverRelacionada(Atividade atividade) {
        if (atividade == null) {
            return "";
        }
        String tipo = atividade instanceof Competicao ? "Competição" : "Evento";
        String data = atividade.getData() == null ? "" : " (" + FORMATO_DATA.format(atividade.getData()) + ")";
        return tipo + " · " + atividade.getTitulo() + data;
    }

    public boolean adicionarResponsavel(Membro membro) {
        return responsaveis.add(membro);
    }

    public boolean removerResponsavel(Membro membro) {
        return responsaveis.remove(membro);
    }

    public void definirResponsaveis(Collection<Membro> membros) {
        responsaveis.clear();
        responsaveis.addAll(membros);
    }

    public Set<Membro> getResponsaveis() {
        return Collections.unmodifiableSet(responsaveis);
    }

    public String getNomesResponsaveis() {
        return responsaveis.stream().map(Membro::getNome).collect(Collectors.joining(", "));
    }

    @Override
    public int compareTo(Demanda outra) {
        return ORDEM.compare(this, outra);
    }

    @Override
    public String toString() {
        return titulo;
    }
}
