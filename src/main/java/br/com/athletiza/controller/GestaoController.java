package br.com.athletiza.controller;

import br.com.athletiza.dao.GestaoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.model.Gestao;
import br.com.athletiza.model.Membro;
import br.com.athletiza.util.Validador;
import java.util.List;
import java.util.Optional;

/**
 * Regras das gestões da atlética e de sua composição (CH-18 a CH-21).
 */
public class GestaoController {

    private final GestaoDAO dao = new GestaoDAO();

    /** Gestões ordenadas por período (ordem natural de Gestao, CH-19). */
    public List<Gestao> listar() throws PersistenciaException {
        return dao.listarTodos().stream().sorted().toList();
    }

    public void salvar(Gestao gestao) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(gestao.getNome(), "Nome da gestão")
                .tamanhoMaximo(gestao.getNome(), 100, "Nome da gestão")
                .obrigatorio(gestao.getDataInicio(), "Data de início")
                .obrigatorio(gestao.getDataFim(), "Data de fim")
                .periodo(gestao.getDataInicio(), gestao.getDataFim(), "Data de início", "Data de fim")
                .obrigatorio(gestao.getSituacao(), "Situação")
                .tamanhoMaximo(gestao.getDescricao(), 500, "Descrição")
                .validar();

        List<Gestao> outras = dao.listarTodos().stream()
                .filter(g -> !g.getId().equals(gestao.getId()))
                .toList();
        Optional<Gestao> sobreposta = outras.stream().filter(gestao::sobrepoe).findFirst();
        if (sobreposta.isPresent()) {
            Gestao outra = sobreposta.get();
            throw new RegraNegocioException("O período informado se sobrepõe à " + outra.getNome() + " ("
                    + Validador.FORMATO_DATA.format(outra.getDataInicio()) + " a "
                    + Validador.FORMATO_DATA.format(outra.getDataFim()) + ").");
        }
        if (outras.stream().anyMatch(g -> g.getNome().equalsIgnoreCase(gestao.getNome().trim()))) {
            throw new RegraNegocioException("Já existe uma gestão chamada " + gestao.getNome() + ".");
        }
        if (gestao.isNova()) {
            dao.inserir(gestao);
        } else {
            dao.atualizar(gestao);
        }
    }

    /** Quantos membros a gestão possui, para confirmar a exclusão (CH-18). */
    public int contarMembros(Gestao gestao) throws PersistenciaException {
        Gestao copia = new Gestao();
        copia.setId(gestao.getId());
        dao.carregarComposicao(copia);
        return copia.getComposicao().size();
    }

    /** Exclui a gestão junto com sua composição (cargos e membros). */
    public void excluir(Gestao gestao) throws PersistenciaException {
        dao.excluir(gestao.getId());
    }

    /** Carrega quem ocupa cada cargo na gestão. */
    public void carregarComposicao(Gestao gestao) throws PersistenciaException {
        dao.carregarComposicao(gestao);
    }

    /** Coloca um membro em um cargo (CH-21). O mesmo membro não pode ocupar o mesmo cargo duas vezes. */
    public void adicionarMembro(Gestao gestao, Membro membro, Cargo cargo)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(cargo, "Cargo").obrigatorio(membro, "Membro").validar();
        if (!gestao.adicionarMembro(membro, cargo)) {
            throw new RegraNegocioException(membro.getNome() + " já ocupa o cargo " + cargo + " nesta gestão.");
        }
        try {
            dao.adicionarMembro(gestao, membro, cargo);
        } catch (PersistenciaException e) {
            gestao.removerMembro(membro, cargo);
            throw e;
        }
    }

    public void removerMembro(Gestao gestao, Membro membro, Cargo cargo) throws PersistenciaException {
        dao.removerMembro(gestao, membro, cargo);
        gestao.removerMembro(membro, cargo);
    }

    /** Troca o ocupante de um cargo (CH-21). */
    public void trocarMembro(Gestao gestao, Cargo cargo, Membro atual, Membro novo)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador().obrigatorio(novo, "Novo membro").validar();
        if (atual.equals(novo)) {
            return;
        }
        if (gestao.getComposicao().stream().anyMatch(mc -> mc.cargo().equals(cargo) && mc.membro().equals(novo))) {
            throw new RegraNegocioException(novo.getNome() + " já ocupa o cargo " + cargo + " nesta gestão.");
        }
        dao.trocarMembro(gestao, cargo, atual, novo);
        gestao.removerMembro(atual, cargo);
        gestao.adicionarMembro(novo, cargo);
    }
}
