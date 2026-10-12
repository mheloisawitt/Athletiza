package br.com.athletiza.controller;

import br.com.athletiza.dao.DemandaDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Atividade;
import br.com.athletiza.model.Demanda;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.SituacaoDemanda;
import br.com.athletiza.util.Validador;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Regras das demandas da atlética: o que precisa ser feito, por quem e,
 * opcionalmente, para qual evento ou competição.
 */
public class DemandaController {

    private final DemandaDAO dao = new DemandaDAO();

    /**
     * Demandas na ordem natural (pendentes primeiro, por prioridade e prazo), com filtros opcionais.
     *
     * @param situacao    apenas demandas nessa situação (null = todas)
     * @param responsavel apenas demandas desse membro (null = de todos)
     */
    public List<Demanda> pesquisar(SituacaoDemanda situacao, Membro responsavel) throws PersistenciaException {
        return dao.listarTodos().stream()
                .filter(d -> situacao == null || d.getSituacao() == situacao)
                .filter(d -> responsavel == null || d.getResponsaveis().contains(responsavel))
                .sorted()
                .toList();
    }

    /**
     * Os responsáveis devem ser membros da gestão em andamento. Quem já era responsável
     * pela demanda pode continuar, mesmo que tenha saído da gestão depois.
     */
    public void salvar(Demanda demanda) throws ValidacaoException, PersistenciaException {
        Set<Membro> permitidos = new HashSet<>(new MembroController().listarDaGestaoEmAndamento());
        if (!demanda.isNova()) {
            dao.buscarPorId(demanda.getId()).ifPresent(salva -> permitidos.addAll(salva.getResponsaveis()));
        }
        List<String> foraDaGestao = demanda.getResponsaveis().stream()
                .filter(m -> !permitidos.contains(m)).map(Membro::getNome).toList();
        new Validador()
                .obrigatorio(demanda.getTitulo(), "Demanda")
                .tamanhoMaximo(demanda.getTitulo(), 100, "Demanda")
                .obrigatorio(demanda.getPrioridade(), "Prioridade")
                .obrigatorio(demanda.getSituacao(), "Situação")
                .tamanhoMaximo(demanda.getDescricao(), 500, "Descrição")
                .regra(foraDaGestao.isEmpty(), "Os responsáveis devem ser membros da gestão em andamento. Não fazem parte dela: "
                        + String.join(", ", foraDaGestao) + ".")
                .validar();
        if (demanda.isNova()) {
            dao.inserir(demanda);
        } else {
            dao.atualizar(demanda);
        }
    }

    /** Exclui a demanda junto com a lista de responsáveis. */
    public void excluir(Demanda demanda) throws PersistenciaException {
        dao.excluir(demanda.getId());
    }

    /** Eventos e competições que podem ser relacionados a uma demanda, em ordem de data. */
    public List<Atividade> listarRelacionaveis() throws PersistenciaException {
        return Stream.<Atividade>concat(
                new EventoController().pesquisar(null, null, null).stream(),
                new CompeticaoController().pesquisar(null, null, null).stream())
                .sorted()
                .toList();
    }
}
