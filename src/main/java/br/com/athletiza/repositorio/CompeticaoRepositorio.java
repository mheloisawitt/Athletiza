package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Competicao;
import br.com.athletiza.modelo.Resultado;
import br.com.athletiza.modelo.Tarefa;
import java.util.List;

public interface CompeticaoRepositorio extends Repositorio<Competicao> {
    void carregarInscricoes(Competicao competicao) throws PersistenciaException;

    void salvarInscricoes(Competicao competicao) throws PersistenciaException;

    List<Resultado> listarResultados(Competicao competicao) throws PersistenciaException;

    void salvarResultado(Competicao competicao, Resultado resultado) throws PersistenciaException;

    List<Tarefa> listarTarefas(Competicao competicao) throws PersistenciaException;

    void salvarTarefa(Competicao competicao, Tarefa tarefa) throws PersistenciaException;

    void excluirTarefa(Tarefa tarefa) throws PersistenciaException;

    void excluirResultado(Resultado resultado) throws PersistenciaException;
}
