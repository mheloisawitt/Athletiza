package br.com.athletiza.modelo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Gestao extends Entidade implements Comparable<Gestao> {
    public static final Comparator<Gestao> POR_PERIODO = Comparator.comparing(Gestao::getDataInicio,
            Comparator.nullsLast(Comparator.naturalOrder()));

    public static final Comparator<Gestao> POR_PERIODO_DECRESCENTE = POR_PERIODO.reversed();

    private String nome;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String descricao;
    private SituacaoGestao situacao = SituacaoGestao.PLANEJADA;
    private final Set<MembroCargo> composicao = new LinkedHashSet<>();

    public Gestao() {
    }

    public Gestao(String nome, LocalDate dataInicio, LocalDate dataFim) {
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public SituacaoGestao getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoGestao situacao) {
        this.situacao = situacao;
    }

    public String getPeriodo() {
        if (dataInicio == null || dataFim == null) {
            return "";
        }
        return dataInicio.getYear() + " - " + dataFim.getYear();
    }

    public boolean sobrepoe(Gestao outra) {
        if (dataInicio == null || dataFim == null || outra.dataInicio == null || outra.dataFim == null) {
            return false;
        }
        return !dataInicio.isAfter(outra.dataFim) && !outra.dataInicio.isAfter(dataFim);
    }

    public boolean adicionarMembro(Membro membro, Cargo cargo) {
        return composicao.add(new MembroCargo(membro, cargo));
    }

    public boolean removerMembro(Membro membro, Cargo cargo) {
        return composicao.remove(new MembroCargo(membro, cargo));
    }

    public Set<MembroCargo> getComposicao() {
        return Collections.unmodifiableSet(composicao);
    }

    public Map<Cargo, List<Membro>> getOrganograma() {
        return composicao.stream().collect(Collectors.groupingBy(MembroCargo::cargo, TreeMap::new,
                Collectors.mapping(MembroCargo::membro, Collectors.toList())));
    }

    @Override
    public int compareTo(Gestao outra) {
        return POR_PERIODO.compare(this, outra);
    }

    @Override
    public String toString() {
        return nome;
    }
}
