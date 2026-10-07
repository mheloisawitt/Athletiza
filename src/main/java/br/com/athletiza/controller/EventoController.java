package br.com.athletiza.controller;

import br.com.athletiza.dao.EventoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Evento;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.model.TipoEvento;
import br.com.athletiza.util.Validador;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras dos eventos organizados pela atlética, responsáveis e tarefas (CH-38 a CH-41).
 */
public class EventoController {

    private final EventoDAO dao = new EventoDAO();

    /**
     * Eventos em ordem de data (ordem natural de Atividade), com filtros opcionais.
     *
     * @param tipo   apenas eventos desse tipo (null = todos)
     * @param inicio apenas eventos a partir desta data (null = sem limite)
     * @param fim    apenas eventos até esta data (null = sem limite)
     */
    public List<Evento> pesquisar(TipoEvento tipo, LocalDate inicio, LocalDate fim) throws PersistenciaException {
        return dao.listarTodos().stream()
                .filter(e -> tipo == null || e.getTipoEvento() == tipo)
                .filter(e -> inicio == null || !e.getData().isBefore(inicio))
                .filter(e -> fim == null || !e.getData().isAfter(fim))
                .sorted()
                .toList();
    }

    public void salvar(Evento evento) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(evento.getTitulo(), "Nome")
                .tamanhoMaximo(evento.getTitulo(), 100, "Nome")
                .obrigatorio(evento.getTipoEvento(), "Tipo")
                .obrigatorio(evento.getData(), "Data")
                .tamanhoMaximo(evento.getLocal(), 100, "Local")
                .obrigatorio(evento.getSituacao(), "Situação")
                .tamanhoMaximo(evento.getDescricao(), 500, "Descrição")
                .tamanhoMaximo(evento.getObservacoes(), 500, "Observações")
                .validar();
        if (evento.isNova()) {
            dao.inserir(evento);
        } else {
            dao.atualizar(evento);
        }
    }

    /** Exclui o evento junto com responsáveis e tarefas. */
    public void excluir(Evento evento) throws PersistenciaException {
        dao.excluir(evento.getId());
    }

    public void carregarDetalhes(Evento evento) throws PersistenciaException {
        dao.carregarDetalhes(evento);
    }

    /** Associa um membro à organização do evento (CH-41). Um membro não pode ser associado duas vezes. */
    public void adicionarResponsavel(Evento evento, Membro membro)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(membro, "Membro").validar();
        if (!evento.adicionarResponsavel(membro)) {
            throw new RegraNegocioException(membro.getNome() + " já é responsável por este evento.");
        }
        try {
            dao.adicionarResponsavel(evento, membro);
        } catch (PersistenciaException e) {
            evento.removerResponsavel(membro);
            throw e;
        }
    }

    public void removerResponsavel(Evento evento, Membro membro) throws PersistenciaException {
        dao.removerResponsavel(evento, membro);
        evento.removerResponsavel(membro);
    }

    /** Inclui ou altera uma tarefa do evento (CH-41). */
    public void salvarTarefa(Evento evento, Tarefa tarefa) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(tarefa.getDescricao(), "Descrição")
                .tamanhoMaximo(tarefa.getDescricao(), 200, "Descrição")
                .obrigatorio(tarefa.getSituacao(), "Situação")
                .validar();
        dao.salvarTarefa(evento, tarefa);
    }

    public void excluirTarefa(Tarefa tarefa) throws PersistenciaException {
        dao.excluirTarefa(tarefa);
    }
}
