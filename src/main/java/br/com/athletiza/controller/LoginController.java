package br.com.athletiza.controller;

import br.com.athletiza.dao.UsuarioRepository;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.model.Validador;
import br.com.athletiza.util.Senha;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.TelaLogin;
import br.com.athletiza.view.TelaPrincipal;
import br.com.athletiza.view.usuarios.DialogoAlterarSenha;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.swing.SwingWorker;

/**
 * Controller da tela de login (CH-12): autentica o usuário e abre a janela principal.
 */
public class LoginController {

    /** Mesma mensagem para login e senha errados, para não revelar quais logins existem. */
    static final String CREDENCIAIS_INVALIDAS = "Usuário ou senha inválidos.";

    /** Mesmo local usado nas versões anteriores (pacote da TelaLogin), para não perder o último login salvo. */
    private static final Preferences PREFERENCIAS = Preferences.userNodeForPackage(TelaLogin.class);
    private static final String ULTIMO_LOGIN = "ultimoLogin";

    private final TelaLogin view;
    private final UsuarioRepository repository;

    public LoginController(TelaLogin view, UsuarioRepository repository) {
        this.view = view;
        this.repository = repository;
        if (view != null) {
            view.setLoginLembrado(PREFERENCIAS.get(ULTIMO_LOGIN, ""));
            adicionarAcoesBotoes();
        }
    }

    public void adicionarAcoesBotoes() {
        view.adicionarAcaoEntrar(e -> entrar());
    }

    /** A senha é conferida em segundo plano (o hash é propositalmente lento), sem travar a tela. */
    private void entrar() {
        String login = view.getLogin();
        char[] senha = view.getSenha();
        view.setEntrando(true);

        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return autenticar(login, senha);
            }

            @Override
            protected void done() {
                Arrays.fill(senha, '\0');
                view.setEntrando(false);
                try {
                    concluirLogin(get(), login);
                } catch (ExecutionException e) {
                    // A exceção lançada em autenticar() chega aqui "embrulhada": a original é a causa
                    view.mostrarErro(e.getCause());
                    view.limparSenha();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /**
     * Confere login e senha e, se estiverem corretos, inicia a sessão.
     *
     * @return o usuário autenticado
     */
    Usuario autenticar(String login, char[] senha) throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(login, "Usuário")
                .regra(senha != null && senha.length > 0, "O campo \"Senha\" é obrigatório.")
                .validar();

        Usuario usuario = repository.buscarPorLogin(login.trim());
        if (usuario == null || !Senha.conferir(senha, usuario.getSenhaHash())) {
            throw new RegraNegocioException(CREDENCIAIS_INVALIDAS);
        }
        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Este usuário está desativado. Procure um administrador.");
        }
        Sessao.iniciar(usuario);
        return usuario;
    }

    /** Troca de senha obrigatória (senha provisória), lembrar o login e abrir a janela principal. */
    private void concluirLogin(Usuario usuario, String login) {
        if (usuario.isDeveTrocarSenha()) {
            DialogoAlterarSenha dialogo = new DialogoAlterarSenha(view,
                    "<html><b>Sua senha é provisória.</b><br>Por segurança, defina uma nova senha para continuar.</html>");
            if (!new AlterarSenhaController(dialogo, usuario, repository).executar("Defina uma nova senha")) {
                Sessao.encerrar();
                view.limparSenha();
                view.mostrarAviso("É preciso definir uma nova senha para entrar no sistema.");
                return;
            }
        }
        if (view.isLembrar()) {
            PREFERENCIAS.put(ULTIMO_LOGIN, login.trim());
        } else {
            PREFERENCIAS.remove(ULTIMO_LOGIN);
        }
        TelaPrincipal principal = new TelaPrincipal();
        new PrincipalController(principal, usuario);
        principal.setVisible(true);
        view.dispose();
    }
}
