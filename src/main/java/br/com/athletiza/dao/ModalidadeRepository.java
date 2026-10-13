package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Modalidade;

/**
 * Acesso às modalidades esportivas (CH-27). Implementada por ModalidadeDAO.
 *
 * Os controllers dependem desta interface (abstração), e não da classe
 * ModalidadeDAO (detalhe): é a Inversão de Dependência (DIP). Nos testes,
 * qualquer outra implementação pode ser usada no lugar do banco.
 */
public interface ModalidadeRepository extends GenericDAO<Modalidade> {

    /** Indica se a modalidade possui atletas, treinos ou competições vinculados (CH-27). */
    boolean possuiVinculos(int id) throws PersistenciaException;
}
