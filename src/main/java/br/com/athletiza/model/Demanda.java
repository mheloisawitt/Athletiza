package br.com.athletiza.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Demanda da atlética: algo que precisa ser feito, com um ou mais membros
 * responsáveis. Pode estar relacionada a um evento (festa, ação social,
 * ação ambiental...) ou a uma competição, mas não é obrigatório.
 *
 * Ordem natural: pendentes antes das encerradas, depois prioridade e prazo.
 */
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
    /** Evento ou competição a que a demanda se refere (null = nenhum). */
    private Atividade relacionada;
    private final Set<Membro> responsaveis = new LinkedHashSet<>();

    public Demanda() {
    }

    public Demanda(String titulo, PrioridadeDemanda prioridade, LocalDate prazo) {
        this.titulo = titulo;
        this.prioridade = prioridade;
        this.prazo = prazo;
    }

    /** Demanda pendente com prazo anterior à data informada. */
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

    public Atividade getRelacionada() {
        return relacionada;
    }

    /**
     * Relaciona a demanda a um evento ou a uma competição (null remove a relação).
     *
     * @throws IllegalArgumentException para outros tipos de atividade (treino, compromisso...)
     */
    public void setRelacionada(Atividade relacionada) {
        if (relacionada != null && !(relacionada instanceof Evento) && !(relacionada instanceof Competicao)) {
            throw new IllegalArgumentException("Uma demanda só pode ser relacionada a um evento ou a uma competição.");
        }
        this.relacionada = relacionada;
    }

    /** Texto da relação para listas e tabelas (ex.: "Evento · Festa Atlética (12/10/2026)"). */
    public String getDescricaoRelacionada() {
        return descreverRelacionada(relacionada);
    }

    /** Texto usado para descrever um evento ou competição que pode ser relacionado a uma demanda. */
    public static String descreverRelacionada(Atividade atividade) {
        if (atividade == null) {
            return "";
        }
        String tipo = atividade instanceof Competicao ? "Competição" : "Evento";
        String data = atividade.getData() == null ? "" : " (" + FORMATO_DATA.format(atividade.getData()) + ")";
        return tipo + " · " + atividade.getTitulo() + data;
    }

    /** @return false se o membro já era responsável pela demanda */
    public boolean adicionarResponsavel(Membro membro) {
        return responsaveis.add(membro);
    }

    public boolean removerResponsavel(Membro membro) {
        return responsaveis.remove(membro);
    }

    /** Troca todos os responsáveis de uma vez (usado pelo cadastro). */
    public void definirResponsaveis(Collection<Membro> membros) {
        responsaveis.clear();
        responsaveis.addAll(membros);
    }

    public Set<Membro> getResponsaveis() {
        return Collections.unmodifiableSet(responsaveis);
    }

    /** Nomes dos responsáveis separados por vírgula, para a tabela. */
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
