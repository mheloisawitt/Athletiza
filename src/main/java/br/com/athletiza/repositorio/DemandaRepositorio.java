package br.com.athletiza.repositorio;

import br.com.athletiza.modelo.Demanda;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.SituacaoDemanda;

public interface DemandaRepositorio extends Repositorio<Demanda> {
    void atualizarSituacao(int id, SituacaoDemanda situacao) throws PersistenciaException;
}
