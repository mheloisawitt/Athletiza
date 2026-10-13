package br.com.athletiza.dao;

import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Atleta;
import br.com.athletiza.model.Frequencia;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.model.PresencaEmTreino;
import br.com.athletiza.model.ResumoPresenca;
import br.com.athletiza.model.Treino;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Acesso aos treinos, amistosos e listas de presença (CH-33). Implementada por TreinoDAO.
 */
public interface TreinoRepository extends GenericDAO<Treino> {

    /** Treinos e amistosos com filtros opcionais (null = sem filtro), em ordem de data. */
    List<Treino> listar(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException;

    /** Treinos no mesmo dia e local, para verificar conflito de horário (CH-35). */
    List<Treino> listarNoMesmoDiaELocal(Treino treino) throws PersistenciaException;

    /** Resumo da presença de cada treino (id do treino -> presentes / registrados). */
    Map<Integer, ResumoPresenca> resumirPresencas() throws PersistenciaException;

    void carregarPresencas(Treino treino) throws PersistenciaException;

    /** Grava a lista de presença inteira de uma vez (CH-36). */
    void salvarPresencas(Treino treino) throws PersistenciaException;

    /** Frequência por atleta nos treinos com presença registrada (CH-37). Filtros nulos são ignorados. */
    List<Frequencia> frequencia(Modalidade modalidade, LocalDate inicio, LocalDate fim) throws PersistenciaException;

    /** Treinos em que o atleta teve presença registrada, do mais recente ao mais antigo (RF13). */
    List<PresencaEmTreino> historicoDoAtleta(Atleta atleta, Modalidade modalidade, LocalDate inicio)
            throws PersistenciaException;
}
