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

/**
 * Campeonato ou competição em que a atlética participa (RF14 a RF16).
 *
 * As inscrições ficam em um Map: modalidade -> atletas inscritos nela.
 * Os atletas de cada modalidade ficam em um Set, impedindo inscrição repetida (CH-31).
 */
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

    /** Competições podem durar vários dias e aparecem em todos eles no calendário. */
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

    /**
     * Inscreve um atleta em uma modalidade da competição.
     *
     * @return false se o atleta já estava inscrito nessa modalidade
     * @throws RegraNegocioException se a modalidade não foi adicionada à competição
     *         ou se o atleta não pratica essa modalidade
     */
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

    /**
     * Restaura uma inscrição já gravada no banco, sem repetir as validações
     * (o atleta pode ter deixado a modalidade depois de inscrito). Uso dos DAOs.
     */
    public void restaurarInscricao(Modalidade modalidade, Atleta atleta) {
        adicionarModalidade(modalidade);
        inscricoes.get(modalidade).add(atleta);
    }

    /** Total de inscrições somando todas as modalidades. */
    public int getTotalInscricoes() {
        return inscricoes.values().stream().mapToInt(Set::size).sum();
    }

    /** Tarefas da organização da competição (RF19), como inscrições e transporte. */
    public void adicionarTarefa(Tarefa tarefa) {
        tarefas.add(tarefa);
    }

    public List<Tarefa> getTarefas() {
        return Collections.unmodifiableList(tarefas);
    }

    public List<Tarefa> getTarefasAtrasadas(LocalDate hoje) {
        return tarefas.stream().filter(t -> t.isAtrasada(hoje)).sorted().toList();
    }

    /** Melhor colocação obtida, para exibir na consulta (ex.: "1º - Futsal (F)"). */
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
