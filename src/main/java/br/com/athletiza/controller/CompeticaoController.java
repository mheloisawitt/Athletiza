package br.com.athletiza.controller;

import br.com.athletiza.dao.CompeticaoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Competicao;
import br.com.athletiza.model.Resultado;
import br.com.athletiza.model.SituacaoCompeticao;
import br.com.athletiza.model.Tarefa;
import br.com.athletiza.util.Validador;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras das competições: cadastro, inscrições e resultados (CH-28 a CH-32).
 */
public class CompeticaoController {

    private final CompeticaoDAO dao = new CompeticaoDAO();

    /**
     * Competições ordenadas por data (ordem natural de Atividade), com filtros opcionais.
     *
     * @param situacao apenas competições nessa situação (null = todas)
     * @param inicio   apenas competições que terminam a partir desta data (null = sem limite)
     * @param fim      apenas competições que começam até esta data (null = sem limite)
     */
    public List<Competicao> pesquisar(SituacaoCompeticao situacao, LocalDate inicio, LocalDate fim)
            throws PersistenciaException {
        return dao.listarTodos().stream()
                .filter(c -> situacao == null || c.getSituacao() == situacao)
                .filter(c -> inicio == null || !terminoDe(c).isBefore(inicio))
                .filter(c -> fim == null || !c.getData().isAfter(fim))
                .sorted()
                .toList();
    }

    public void salvar(Competicao competicao) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(competicao.getTitulo(), "Nome")
                .tamanhoMaximo(competicao.getTitulo(), 100, "Nome")
                .obrigatorio(competicao.getData(), "Data de início")
                .periodo(competicao.getData(), competicao.getDataFim(), "Data de início", "Data de fim")
                .tamanhoMaximo(competicao.getLocal(), 100, "Local")
                .obrigatorio(competicao.getSituacao(), "Situação")
                .tamanhoMaximo(competicao.getObservacoes(), 500, "Observações")
                .validar();
        if (competicao.isNova()) {
            dao.inserir(competicao);
        } else {
            dao.atualizar(competicao);
        }
    }

    /** Exclui a competição junto com inscrições, resultados e tarefas (em cascata no banco). */
    public void excluir(Competicao competicao) throws PersistenciaException {
        dao.excluir(competicao.getId());
    }

    public void carregarInscricoes(Competicao competicao) throws PersistenciaException {
        dao.carregarInscricoes(competicao);
    }

    /** Grava modalidades e atletas inscritos, montados na tela com os métodos de Competicao (CH-31). */
    public void salvarInscricoes(Competicao competicao) throws PersistenciaException {
        dao.salvarInscricoes(competicao);
    }

    public List<Resultado> listarResultados(Competicao competicao) throws PersistenciaException {
        return dao.listarResultados(competicao);
    }

    /**
     * Registra ou altera um resultado (CH-32). A modalidade precisa estar inscrita e,
     * se o resultado for individual, o atleta precisa estar inscrito nela.
     */
    public void salvarResultado(Competicao competicao, Resultado resultado)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(resultado.getModalidade(), "Modalidade")
                .regra(resultado.getColocacao() == null || resultado.getColocacao() > 0,
                        "A \"Colocação\" deve ser maior que zero.")
                .regra(resultado.getColocacao() != null
                        || (resultado.getPlacar() != null && !resultado.getPlacar().isBlank()),
                        "Informe a colocação ou o placar.")
                .tamanhoMaximo(resultado.getPlacar(), 50, "Placar")
                .tamanhoMaximo(resultado.getObservacao(), 500, "Observação")
                .validar();

        if (!competicao.getModalidades().contains(resultado.getModalidade())) {
            throw new RegraNegocioException("A modalidade " + resultado.getModalidade() + " não está inscrita nesta competição.");
        }
        if (resultado.getAtleta() != null
                && !competicao.getAtletas(resultado.getModalidade()).contains(resultado.getAtleta())) {
            throw new RegraNegocioException(resultado.getAtleta().getNome() + " não está inscrito(a) em "
                    + resultado.getModalidade() + " nesta competição.");
        }
        dao.salvarResultado(competicao, resultado);
    }

    /** Tarefas da organização da competição, por prazo (RF19). */
    public List<Tarefa> listarTarefas(Competicao competicao) throws PersistenciaException {
        return dao.listarTarefas(competicao).stream().sorted().toList();
    }

    public void salvarTarefa(Competicao competicao, Tarefa tarefa) throws ValidacaoException, PersistenciaException {
        new Validador()
                .obrigatorio(tarefa.getDescricao(), "Descrição")
                .tamanhoMaximo(tarefa.getDescricao(), 200, "Descrição")
                .obrigatorio(tarefa.getSituacao(), "Situação")
                .validar();
        dao.salvarTarefa(competicao, tarefa);
    }

    public void excluirTarefa(Tarefa tarefa) throws PersistenciaException {
        dao.excluirTarefa(tarefa);
    }

    public void excluirResultado(Resultado resultado) throws PersistenciaException {
        dao.excluirResultado(resultado);
    }

    private static LocalDate terminoDe(Competicao competicao) {
        return competicao.getDataFim() == null ? competicao.getData() : competicao.getDataFim();
    }
}
