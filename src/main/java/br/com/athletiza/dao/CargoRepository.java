package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Cargo;

/**
 * Acesso aos cargos da gestão (CH-18). Implementada por CargoDAO.
 */
public interface CargoRepository extends GenericDAO<Cargo> {

    /** Quantas vezes o cargo está ocupado, somando todas as gestões. */
    int contarOcupacoes(int id) throws PersistenciaException;
}
