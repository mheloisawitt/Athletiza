package br.com.athletiza.repositorio;

import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.excecao.PersistenciaException;
import java.util.List;

public interface AtletaRepositorio extends Repositorio<Atleta> {
    boolean existeMatricula(String matricula, Integer idIgnorado) throws PersistenciaException;

    List<String> listarVinculos(int id) throws PersistenciaException;
}
