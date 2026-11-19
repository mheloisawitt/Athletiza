package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Atividade;
import java.time.LocalDate;
import java.util.List;

public interface AtividadeRepositorio {
    List<Atividade> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException;
}
