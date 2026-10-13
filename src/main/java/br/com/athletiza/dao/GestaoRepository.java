package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;

/**
 * Acesso às gestões e à sua composição (quem ocupa cada cargo, CH-18).
 * Implementada por GestaoDAO.
 */
public interface GestaoRepository extends GenericDAO<Gestao> {

    /** Preenche a composição da gestão (membro x cargo). */
    void carregarComposicao(Gestao gestao) throws PersistenciaException;

    void adicionarMembro(Gestao gestao, Membro membro, Cargo cargo) throws PersistenciaException;

    void removerMembro(Gestao gestao, Membro membro, Cargo cargo) throws PersistenciaException;

    /** Troca quem ocupa um cargo, em uma única transação. */
    void trocarMembro(Gestao gestao, Cargo cargo, Membro atual, Membro novo) throws PersistenciaException;
}
