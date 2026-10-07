package br.com.athletiza.controller;

import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Senha;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Cadastro de usuários do sistema e troca de senha (RF01, CH-13).
 * O sistema nunca fica sem um administrador ativo.
 */
public class UsuarioController {

    static final int TAMANHO_MINIMO_SENHA = 6;
    private static final Pattern LOGIN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

    private final UsuarioDAO dao = new UsuarioDAO();

    public List<Usuario> listar() throws PersistenciaException {
        return dao.listarTodos();
    }

    /**
     * Inclui ou altera um usuário. Na inclusão a senha é obrigatória; na alteração,
     * só é trocada se for informada (redefinição feita pelo administrador).
     */
    public void salvar(Usuario usuario, char[] senha, char[] confirmacao)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        boolean informouSenha = senha != null && senha.length > 0;
        Validador validador = new Validador()
                .obrigatorio(usuario.getNome(), "Nome")
                .tamanhoMaximo(usuario.getNome(), 100, "Nome")
                .obrigatorio(usuario.getLogin(), "Login")
                .regra(usuario.getLogin() == null || usuario.getLogin().isBlank() || LOGIN.matcher(usuario.getLogin()).matches(),
                        "O \"Login\" deve ter de 3 a 50 caracteres, sem espaços (letras, números, ponto, hífen ou _).")
                .obrigatorio(usuario.getPerfil(), "Perfil")
                .regra(!usuario.isNova() || informouSenha, "O campo \"Senha\" é obrigatório para novos usuários.");
        if (informouSenha) {
            validarNovaSenha(validador, senha, confirmacao);
        }
        validador.validar();

        if (dao.existeLogin(usuario.getLogin(), usuario.getId())) {
            throw new RegraNegocioException("O login \"" + usuario.getLogin() + "\" já está em uso.");
        }
        boolean deixaDeSerAdministradorAtivo = usuario.getPerfil() != Perfil.ADMINISTRADOR || !usuario.isAtivo();
        if (!usuario.isNova() && deixaDeSerAdministradorAtivo && dao.contarOutrosAdministradoresAtivos(usuario.getId()) == 0) {
            throw new RegraNegocioException("O sistema precisa de pelo menos um administrador ativo.");
        }
        if (informouSenha) {
            usuario.setSenhaHash(Senha.gerarHash(senha));
        }
        if (usuario.isNova()) {
            dao.inserir(usuario);
        } else {
            dao.atualizar(usuario);
        }
    }

    /** Troca da própria senha: exige a senha atual. */
    public void alterarSenha(Usuario usuario, char[] senhaAtual, char[] nova, char[] confirmacao)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        Validador validador = new Validador()
                .regra(senhaAtual != null && senhaAtual.length > 0, "O campo \"Senha atual\" é obrigatório.");
        validarNovaSenha(validador, nova, confirmacao);
        validador.validar();

        Usuario gravado = dao.buscarPorId(usuario.getId())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
        if (!Senha.conferir(senhaAtual, gravado.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual não confere.");
        }
        if (Senha.conferir(nova, gravado.getSenhaHash())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da atual.");
        }
        gravado.setSenhaHash(Senha.gerarHash(nova));
        dao.atualizar(gravado);
        usuario.setSenhaHash(gravado.getSenhaHash());
    }

    /** Exclui um usuário; não é possível excluir a si mesmo nem o último administrador. */
    public void excluir(Usuario usuario) throws RegraNegocioException, PersistenciaException {
        if (usuario.equals(Sessao.getUsuarioLogado())) {
            throw new RegraNegocioException("Você não pode excluir o seu próprio usuário.");
        }
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR && dao.contarOutrosAdministradoresAtivos(usuario.getId()) == 0) {
            throw new RegraNegocioException("O sistema precisa de pelo menos um administrador ativo.");
        }
        dao.excluir(usuario.getId());
    }

    private static void validarNovaSenha(Validador validador, char[] senha, char[] confirmacao) {
        validador
                .regra(senha != null && senha.length >= TAMANHO_MINIMO_SENHA,
                        "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.")
                .regra(Arrays.equals(senha, confirmacao), "A confirmação não confere com a senha.");
    }
}
