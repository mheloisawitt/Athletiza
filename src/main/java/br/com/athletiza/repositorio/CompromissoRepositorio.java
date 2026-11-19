package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Compromisso;
import java.time.LocalDate;
import java.util.List;

public interface CompromissoRepositorio extends Repositorio<Compromisso> {
    List<Compromisso> listarPorPeriodo(LocalDate inicio, LocalDate fim) throws PersistenciaException;
}
