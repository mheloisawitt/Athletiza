package br.com.athletiza.model;

import br.com.athletiza.exception.RegraNegocioException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Competicao extends Atividade {
    private LocalDate dataFim;
    private SituacaoCompeticao situacao = SituacaoCompeticao.PLANEJADA;
    private final Map<Modalidade, Set<Atleta>> inscricoes = new LinkedHashMap<>();
    private final List<Resultado> resultados = new ArrayList<>();
    private final List<Tarefa> tarefas = new ArrayList<>();

    public Competicao() {
    }

    public Competicao(String titulo, LocalDate dataInicio, LocalDate dataFim, LocalTime horario, String local) {
        super(titulo, dataInicio, horario, local);
        this.dataFim = dataFim;
    }

    @Override
    public TipoAtividade getTipo() {
        return TipoAtividade.COMPETICAO;
    }

    @Override
    public boolean ocorreEm(LocalDate dia) {
        if (getData() == null || dia == null) {
            return false;
        }
        LocalDate fim = dataFim == null ? getData() : dataFim;
        return !dia.isBefore(getData()) && !dia.isAfter(fim);
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public SituacaoCompeticao getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoCompeticao situacao) {
        this.situacao = situacao;
    }

    public void adicionarModalidade(Modalidade modalidade) {
        inscricoes.putIfAbsent(modalidade, new LinkedHashSet<>());
    }

    public void removerModalidade(Modalidade modalidade) {
        inscricoes.remove(modalidade);
    }

    public Set<Modalidade> getModalidades() {
        return Collections.unmodifiableSet(inscricoes.keySet());
    }

    public boolean inscreverAtleta(Modalidade modalidade, Atleta atleta) throws RegraNegocioException {
        Set<Atleta> atletas = inscricoes.get(modalidade);
        if (atletas == null) {
            throw new RegraNegocioException("A modalidade " + modalidade + " não faz parte desta competição.");
        }
        if (!atleta.praticaModalidade(modalidade)) {
            throw new RegraNegocioException(atleta.getNome() + " não está vinculado(a) à modalidade " + modalidade + ".");
        }
        return atletas.add(atleta);
    }

    public void restaurarInscricao(Modalidade modalidade, Atleta atleta) {
        adicionarModalidade(modalidade);
        inscricoes.get(modalidade).add(atleta);
    }

    public int getTotalInscricoes() {
        return inscricoes.values().stream().mapToInt(Set::size).sum();
    }

    public void adicionarTarefa(Tarefa tarefa) {
        tarefas.add(tarefa);
    }

    public List<Tarefa> getTarefas() {
        return Collections.unmodifiableList(tarefas);
    }

    public List<Tarefa> getTarefasAtrasadas(LocalDate hoje) {
        return tarefas.stream().filter(t -> t.isAtrasada(hoje)).sorted().toList();
    }

    public String getMelhorResultado() {
        return resultados.stream()
                .filter(r -> r.getColocacao() != null)
                .min(java.util.Comparator.comparing(Resultado::getColocacao))
                .map(r -> r.getColocacao() + "º - " + r.getModalidade())
                .orElse(resultados.isEmpty() ? "" : resultados.size() + " lançado(s)");
    }

    public boolean removerAtleta(Modalidade modalidade, Atleta atleta) {
        Set<Atleta> atletas = inscricoes.get(modalidade);
        return atletas != null && atletas.remove(atleta);
    }

    public Set<Atleta> getAtletas(Modalidade modalidade) {
        return Collections.unmodifiableSet(inscricoes.getOrDefault(modalidade, Set.of()));
    }

    public void adicionarResultado(Resultado resultado) {
        resultados.add(resultado);
    }

    public List<Resultado> getResultados() {
        return Collections.unmodifiableList(resultados);
    }
}
