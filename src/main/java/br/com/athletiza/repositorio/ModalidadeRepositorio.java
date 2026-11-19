package br.com.athletiza.repositorio;

import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.excecao.PersistenciaException;

public interface ModalidadeRepositorio extends Repositorio<Modalidade> {
    boolean possuiVinculos(int id) throws PersistenciaException;
}
