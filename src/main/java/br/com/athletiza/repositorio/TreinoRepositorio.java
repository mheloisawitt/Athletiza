package br.com.athletiza.repositorio;

import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.modelo.Atleta;
import br.com.athletiza.modelo.Frequencia;
import br.com.athletiza.modelo.Modalidade;
import br.com.athletiza.modelo.PresencaEmTreino;
import br.com.athletiza.modelo.Treino;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TreinoRepositorio extends Repositorio<Treino> {
    record ResumoPresenca(int presentes, int registrados) {
    }

    List<Treino> listar(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException;

    List<Treino> listarNoMesmoDiaELocal(Treino treino) throws PersistenciaException;

    Map<Integer, ResumoPresenca> resumirPresencas() throws PersistenciaException;

    void carregarPresencas(Treino treino) throws PersistenciaException;

    void salvarPresencas(Treino treino) throws PersistenciaException;

    List<Frequencia> frequencia(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException;

    List<PresencaEmTreino> historicoDoAtleta(Atleta atleta, Modalidade modalidade, LocalDate inicio) throws PersistenciaException;
}
