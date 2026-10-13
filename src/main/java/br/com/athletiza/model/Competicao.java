package br.com.athletiza.model;

import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
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

    /** Regras de preenchimento do cadastro (CH-30). */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(getTitulo(), "Nome")
                .tamanhoMaximo(getTitulo(), 100, "Nome")
                .obrigatorio(getData(), "Data de início")
                .periodo(getData(), dataFim, "Data de início", "Data de fim")
                .tamanhoMaximo(getLocal(), 100, "Local")
                .obrigatorio(situacao, "Situação")
                .tamanhoMaximo(getObservacoes(), 500, "Observações")
                .validar();
    }

    /** Competições podem durar vários dias e aparecem em todos eles no calendário. */
    @Override
    public boolean ocorreEm(LocalDate dia) {
        if (getData() == null || dia == null) {
            return false;
        }
        return !dia.isBefore(getData()) && !dia.isAfter(getTermino());
    }

    /** Último dia da competição (a data de início, se ela durar um dia só). */
    public LocalDate getTermino() {
        return dataFim == null ? getData() : dataFim;
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
        if (!inscricoes.containsKey(modalidade)) {
            inscricoes.put(modalidade, new LinkedHashSet<>());
        }
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
        int total = 0;
        for (Set<Atleta> atletas : inscricoes.values()) {
            total += atletas.size();
        }
        return total;
    }

    /**
     * Confere se o resultado pode ser lançado (CH-32): a modalidade precisa estar
     * inscrita e, se o resultado for individual, o atleta precisa estar inscrito nela.
     */
    public void verificarResultado(Resultado resultado) throws RegraNegocioException {
        if (!inscricoes.containsKey(resultado.getModalidade())) {
            throw new RegraNegocioException("A modalidade " + resultado.getModalidade()
                    + " não está inscrita nesta competição.");
        }
        if (resultado.getAtleta() != null && !getAtletas(resultado.getModalidade()).contains(resultado.getAtleta())) {
            throw new RegraNegocioException(resultado.getAtleta().getNome() + " não está inscrito(a) em "
                    + resultado.getModalidade() + " nesta competição.");
        }
    }

    /**
     * Atletas que aparecem na lista de inscrição de uma modalidade: os que
     * praticam a modalidade e os que já estão inscritos nela (mesmo que tenham saído).
     */
    public List<Atleta> getAtletasDisponiveis(Modalidade modalidade, List<Atleta> todosAtletas) {
        Set<Atleta> disponiveis = new LinkedHashSet<>();
        for (Atleta atleta : todosAtletas) {
            if (atleta.praticaModalidade(modalidade)) {
                disponiveis.add(atleta);
            }
        }
        disponiveis.addAll(getAtletas(modalidade));
        return new ArrayList<>(disponiveis);
    }

    /** Cópia com os mesmos dados, mas sem inscrições, resultados e tarefas (para ler de novo do banco). */
    public Competicao copiarDados() {
        Competicao copia = new Competicao(getTitulo(), getData(), dataFim, getHorario(), getLocal());
        copia.setId(getId());
        copia.setSituacao(situacao);
        copia.setObservacoes(getObservacoes());
        return copia;
    }

    /** Tarefas da organização da competição (RF19), como inscrições e transporte. */
    public void adicionarTarefa(Tarefa tarefa) {
        tarefas.add(tarefa);
    }

    public List<Tarefa> getTarefas() {
        return Collections.unmodifiableList(tarefas);
    }

    public List<Tarefa> getTarefasAtrasadas(LocalDate hoje) {
        List<Tarefa> atrasadas = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            if (tarefa.isAtrasada(hoje)) {
                atrasadas.add(tarefa);
            }
        }
        Collections.sort(atrasadas);
        return atrasadas;
    }

    /** Melhor colocação obtida, para exibir na consulta (ex.: "1º - Futsal (F)"). */
    public String getMelhorResultado() {
        Resultado melhor = null;
        for (Resultado resultado : resultados) {
            if (resultado.getColocacao() != null
                    && (melhor == null || resultado.getColocacao() < melhor.getColocacao())) {
                melhor = resultado;
            }
        }
        if (melhor != null) {
            return melhor.getColocacao() + "º - " + melhor.getModalidade();
        }
        return resultados.isEmpty() ? "" : resultados.size() + " lançado(s)";
    }

    public boolean removerAtleta(Modalidade modalidade, Atleta atleta) {
        Set<Atleta> atletas = inscricoes.get(modalidade);
        return atletas != null && atletas.remove(atleta);
    }

    public Set<Atleta> getAtletas(Modalidade modalidade) {
        Set<Atleta> atletas = inscricoes.get(modalidade);
        if (atletas == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(atletas);
    }

    public void adicionarResultado(Resultado resultado) {
        resultados.add(resultado);
    }

    public List<Resultado> getResultados() {
        return Collections.unmodifiableList(resultados);
    }
}
