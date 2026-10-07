package br.com.athletiza.controller;

import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Senha;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;

/**
 * Autenticação de usuários (CH-12).
 */
public class LoginController {

    /** Mesma mensagem para login e senha errados, para não revelar quais logins existem. */
    static final String CREDENCIAIS_INVALIDAS = "Usuário ou senha inválidos.";

    private final UsuarioDAO usuarioDAO;

    public LoginController() {
        this(new UsuarioDAO());
    }

    LoginController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Confere login e senha e, se estiverem corretos, inicia a sessão.
     *
     * @return o usuário autenticado
     */
    public Usuario autenticar(String login, char[] senha)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(login, "Usuário")
                .regra(senha != null && senha.length > 0, "O campo \"Senha\" é obrigatório.")
                .validar();

        Usuario usuario = usuarioDAO.buscarPorLogin(login.trim())
                .filter(u -> Senha.conferir(senha, u.getSenhaHash()))
                .orElseThrow(() -> new RegraNegocioException(CREDENCIAIS_INVALIDAS));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Este usuário está desativado. Procure um administrador.");
        }
        Sessao.iniciar(usuario);
        return usuario;
    }

    public void sair() {
        Sessao.encerrar();
    }
}
