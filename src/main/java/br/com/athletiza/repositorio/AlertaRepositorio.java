package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Alerta;
import java.time.LocalDate;
import java.util.List;

public interface AlertaRepositorio {
    List<Alerta> tarefasAtrasadas(LocalDate hoje) throws PersistenciaException;

    List<Alerta> treinosSemPresenca(LocalDate hoje, int dias) throws PersistenciaException;

    List<Alerta> competicoesProximas(LocalDate hoje, int dias) throws PersistenciaException;
}
