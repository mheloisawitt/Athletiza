package br.com.athletiza.controller;

import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Cargo;
import br.com.athletiza.util.Validador;
import java.util.List;

/**
 * Cadastro dos cargos da gestão e da hierarquia entre eles (CH-18, RF07).
 */
public class CargoController {

    private final CargoDAO dao = new CargoDAO();

    /** Cargos em ordem hierárquica (nível e nome). */
    public List<Cargo> listar() throws PersistenciaException {
        return dao.listarTodos().stream().sorted().toList();
    }

    public void salvar(Cargo cargo) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(cargo.getNome(), "Nome")
                .tamanhoMaximo(cargo.getNome(), 60, "Nome")
                .regra(cargo.getOrdem() >= 1, "O \"Nível\" deve ser 1 ou maior (1 = topo do organograma).")
                .validar();

        Cargo superior = cargo.getCargoSuperior();
        if (superior != null) {
            if (superior.getOrdem() >= cargo.getOrdem()) {
                throw new RegraNegocioException("O cargo superior (" + superior + ", nível " + superior.getOrdem()
                        + ") deve ter nível menor que o deste cargo.");
            }
            for (Cargo acima = superior; acima != null; acima = acima.getCargoSuperior()) {
                if (!cargo.isNova() && cargo.getId().equals(acima.getId())) {
                    throw new RegraNegocioException("Um cargo não pode ficar subordinado a si mesmo.");
                }
            }
        }
        boolean repetido = dao.listarTodos().stream()
                .anyMatch(c -> c.equals(cargo) && !c.getId().equals(cargo.getId()));
        if (repetido) {
            throw new RegraNegocioException("O cargo " + cargo.getNome() + " já está cadastrado.");
        }
        if (cargo.isNova()) {
            dao.inserir(cargo);
        } else {
            dao.atualizar(cargo);
        }
    }

    public void excluir(Cargo cargo) throws RegraNegocioException, PersistenciaException {
        int ocupacoes = dao.contarOcupacoes(cargo.getId());
        if (ocupacoes > 0) {
            throw new RegraNegocioException("Não é possível excluir o cargo " + cargo.getNome()
                    + ": ele está ocupado " + ocupacoes + " vez(es) em gestões.");
        }
        dao.excluir(cargo.getId());
    }
}
