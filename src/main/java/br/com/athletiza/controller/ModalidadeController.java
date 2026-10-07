package br.com.athletiza.controller;

import br.com.athletiza.dao.ModalidadeDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Modalidade;
import br.com.athletiza.util.Validador;
import java.util.List;

/**
 * Cadastro das modalidades esportivas (CH-27).
 */
public class ModalidadeController {

    private final ModalidadeDAO dao = new ModalidadeDAO();

    public List<Modalidade> listar() throws PersistenciaException {
        return dao.listarTodos();
    }

    public void salvar(Modalidade modalidade) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(modalidade.getNome(), "Nome")
                .tamanhoMaximo(modalidade.getNome(), 50, "Nome")
                .obrigatorio(modalidade.getGenero(), "Gênero")
                .validar();

        boolean repetida = dao.listarTodos().stream()
                .anyMatch(m -> m.equals(modalidade) && !m.getId().equals(modalidade.getId()));
        if (repetida) {
            throw new RegraNegocioException("A modalidade " + modalidade + " já está cadastrada.");
        }
        if (modalidade.isNova()) {
            dao.inserir(modalidade);
        } else {
            dao.atualizar(modalidade);
        }
    }

    /** Só exclui modalidades sem atletas, treinos ou competições vinculados. */
    public void excluir(Modalidade modalidade) throws RegraNegocioException, PersistenciaException {
        if (dao.possuiVinculos(modalidade.getId())) {
            throw new RegraNegocioException("Não é possível excluir a modalidade " + modalidade
                    + ": existem atletas, treinos ou competições vinculados a ela.");
        }
        dao.excluir(modalidade.getId());
    }
}
