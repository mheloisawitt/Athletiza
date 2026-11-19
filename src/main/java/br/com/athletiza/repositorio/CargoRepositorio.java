package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Cargo;
import java.util.List;

public interface CargoRepositorio extends Repositorio<Cargo> {
    List<Cargo> listarDoMembroNaGestaoEmAndamento(int membroId) throws PersistenciaException;

    int contarOcupacoes(int id) throws PersistenciaException;
}
