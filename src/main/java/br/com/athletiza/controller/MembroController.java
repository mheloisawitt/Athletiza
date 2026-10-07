package br.com.athletiza.controller;

import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.util.Validador;
import java.util.List;

/**
 * Cadastro dos membros da atlética (CH-22).
 */
public class MembroController {

    private final MembroDAO dao = new MembroDAO();

    /** Membros em ordem alfabética. */
    public List<Membro> listar() throws PersistenciaException {
        return dao.listarTodos();
    }

    /** Membros ativos, para escolher ocupantes de cargos e responsáveis. */
    public List<Membro> listarAtivos() throws PersistenciaException {
        return dao.listarTodos().stream().filter(m -> m.getSituacao() == Situacao.ATIVO).toList();
    }

    public void salvar(Membro membro) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(membro.getNome(), "Nome")
                .tamanhoMaximo(membro.getNome(), 100, "Nome")
                .obrigatorio(membro.getMatricula(), "Matrícula")
                .apenasNumeros(membro.getMatricula(), "Matrícula")
                .tamanhoMaximo(membro.getMatricula(), 20, "Matrícula")
                .contato(membro.getContato(), "Contato")
                .tamanhoMaximo(membro.getContato(), 100, "Contato")
                .obrigatorio(membro.getSituacao(), "Situação")
                .validar();

        if (dao.existeMatricula(membro.getMatricula(), membro.getId())) {
            throw new RegraNegocioException("Já existe um membro com a matrícula " + membro.getMatricula() + ".");
        }
        if (membro.isNova()) {
            dao.inserir(membro);
        } else {
            dao.atualizar(membro);
        }
    }

    public void excluir(Membro membro) throws RegraNegocioException, PersistenciaException {
        List<String> vinculos = dao.listarVinculos(membro.getId());
        if (!vinculos.isEmpty()) {
            throw new RegraNegocioException("Não é possível excluir " + membro.getNome() + ", pois possui:\n- "
                    + String.join("\n- ", vinculos)
                    + "\n\nPara retirá-lo(a) das listagens, altere a situação para Inativo.");
        }
        dao.excluir(membro.getId());
    }
}
