package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Cargo;
import br.com.athletiza.modelo.Gestao;
import br.com.athletiza.modelo.Membro;

public interface GestaoRepositorio extends Repositorio<Gestao> {
    void carregarComposicao(Gestao gestao) throws PersistenciaException;

    void adicionarMembro(Gestao gestao, Membro membro, Cargo cargo) throws PersistenciaException;

    void removerMembro(Gestao gestao, Membro membro, Cargo cargo) throws PersistenciaException;

    void trocarMembro(Gestao gestao, Cargo cargo, Membro atual, Membro novo) throws PersistenciaException;
}
