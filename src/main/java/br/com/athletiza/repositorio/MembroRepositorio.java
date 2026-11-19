package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Membro;
import java.util.List;

public interface MembroRepositorio extends Repositorio<Membro> {
    List<Membro> listarDaGestaoEmAndamento() throws PersistenciaException;

    boolean existeMatricula(String matricula, Integer idIgnorado) throws PersistenciaException;

    List<String> listarVinculos(int id) throws PersistenciaException;
}
