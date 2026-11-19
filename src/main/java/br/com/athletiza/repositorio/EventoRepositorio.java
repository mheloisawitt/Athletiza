package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Evento;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Tarefa;

public interface EventoRepositorio extends Repositorio<Evento> {
    void carregarDetalhes(Evento evento) throws PersistenciaException;

    void adicionarResponsavel(Evento evento, Membro membro) throws PersistenciaException;

    void removerResponsavel(Evento evento, Membro membro) throws PersistenciaException;

    void salvarTarefa(Evento evento, Tarefa tarefa) throws PersistenciaException;

    void excluirTarefa(Tarefa tarefa) throws PersistenciaException;
}
