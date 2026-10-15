package br.com.athletiza.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Atleta da atlética (RF03), vinculado a uma ou mais modalidades,
 * cada uma com a sua situação (RF04).
 *
 * Ordem natural: pelo nome. Ordenações alternativas nos Comparators abaixo (CH-11).
 */
public class Atleta extends Pessoa implements Comparable<Atleta> {

    public static final Comparator<Atleta> POR_NOME = Comparator.comparing(Atleta::getNome, Textos.COMPARADOR);

    public static final Comparator<Atleta> POR_MATRICULA = Comparator.comparing(Atleta::getMatricula,
            Comparator.nullsLast(Comparator.naturalOrder()));

    public static final Comparator<Atleta> POR_SITUACAO = Comparator.comparing(Atleta::getSituacao,
            Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(POR_NOME);

    /** Pela primeira modalidade (em ordem alfabética); atletas sem modalidade ficam no final. */
    public static final Comparator<Atleta> POR_MODALIDADE = Comparator.comparing(
            (Atleta a) -> a.getModalidadePrincipal().orElse(null),
            Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(POR_NOME);

    private LocalDate dataNascimento;
    private Situacao situacao = Situacao.ATIVO;
    private String observacoes;
    private final Map<Modalidade, SituacaoAtleta> modalidades = new LinkedHashMap<>();

    public Atleta() {
    }

    public Atleta(String nome, String matricula, String contato) {
        super(nome, matricula, contato);
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    /** Vincula o atleta à modalidade ou atualiza a situação, se já estiver vinculado. */
    public void vincularModalidade(Modalidade modalidade, SituacaoAtleta situacaoNaModalidade) {
        modalidades.put(modalidade, situacaoNaModalidade);
    }

    public void desvincularModalidade(Modalidade modalidade) {
        modalidades.remove(modalidade);
    }

    public boolean praticaModalidade(Modalidade modalidade) {
        return modalidades.containsKey(modalidade);
    }

    public Optional<SituacaoAtleta> getSituacaoNaModalidade(Modalidade modalidade) {
        return Optional.ofNullable(modalidades.get(modalidade));
    }

    /** Modalidades do atleta, sem repetição e em ordem alfabética. */
    public Set<Modalidade> getModalidades() {
        return Collections.unmodifiableSet(new TreeSet<>(modalidades.keySet()));
    }

    public Map<Modalidade, SituacaoAtleta> getVinculos() {
        return Collections.unmodifiableMap(modalidades);
    }

    public Optional<Modalidade> getModalidadePrincipal() {
        return modalidades.keySet().stream().min(Comparator.naturalOrder());
    }

    @Override
    public int compareTo(Atleta outro) {
        return POR_NOME.compare(this, outro);
    }
}
