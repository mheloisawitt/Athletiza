package br.com.athletiza.model;

import br.com.athletiza.exception.ValidacaoException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Evento organizado pela atlética: festa, ação social, ação ambiental... (RF17 a RF19).
 */
public class Evento extends Atividade {

    private TipoEvento tipoEvento;
    private SituacaoEvento situacao = SituacaoEvento.PLANEJADO;
    private String descricao;
    private final Set<Membro> responsaveis = new LinkedHashSet<>();
    private final List<Tarefa> tarefas = new ArrayList<>();

    public Evento() {
    }

    public Evento(String titulo, TipoEvento tipoEvento, LocalDate data, LocalTime horario, String local) {
        super(titulo, data, horario, local);
        this.tipoEvento = tipoEvento;
    }

    @Override
    public TipoAtividade getTipo() {
        return TipoAtividade.EVENTO;
    }

    /** Regras de preenchimento do cadastro (CH-40). */
    public void validar() throws ValidacaoException {
        new Validador()
                .obrigatorio(getTitulo(), "Nome")
                .tamanhoMaximo(getTitulo(), 100, "Nome")
                .obrigatorio(tipoEvento, "Tipo")
                .obrigatorio(getData(), "Data")
                .tamanhoMaximo(getLocal(), 100, "Local")
                .obrigatorio(situacao, "Situação")
                .tamanhoMaximo(descricao, 500, "Descrição")
                .tamanhoMaximo(getObservacoes(), 500, "Observações")
                .validar();
    }

    @Override
    public String getDescricaoCalendario() {
        return (tipoEvento == null ? "Evento" : tipoEvento.getDescricao()) + " - " + getTitulo();
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public SituacaoEvento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoEvento situacao) {
        this.situacao = situacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /** @return false se o membro já era responsável pelo evento */
    public boolean adicionarResponsavel(Membro membro) {
        return responsaveis.add(membro);
    }

    public boolean removerResponsavel(Membro membro) {
        return responsaveis.remove(membro);
    }

    public Set<Membro> getResponsaveis() {
        return Collections.unmodifiableSet(responsaveis);
    }

    public void adicionarTarefa(Tarefa tarefa) {
        tarefas.add(tarefa);
    }

    public boolean removerTarefa(Tarefa tarefa) {
        return tarefas.remove(tarefa);
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
}
