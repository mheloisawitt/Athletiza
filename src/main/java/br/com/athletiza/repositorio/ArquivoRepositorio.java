package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Arquivo;
import br.com.athletiza.modelo.Pasta;
import java.util.List;

public interface ArquivoRepositorio extends Repositorio<Arquivo> {
    List<Arquivo> listarPorPasta(Pasta pasta) throws PersistenciaException;
}
