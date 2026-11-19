package br.com.athletiza.controlador;

import br.com.athletiza.excecao.AthletizaException;
import br.com.athletiza.excecao.PersistenciaException;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Acesso;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.repositorio.CargoRepositorio;
import br.com.athletiza.repositorio.UsuarioRepositorio;
import br.com.athletiza.util.Senha;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.util.Validador;
import br.com.athletiza.visao.TelaLogin;
import br.com.athletiza.visao.TelaPrincipal;
import br.com.athletiza.visao.usuarios.DialogoAlterarSenha;
import java.util.Arrays;

public class LoginControlador {
    static final String CREDENCIAIS_INVALIDAS = "Usuário ou senha inválidos.";

    private TelaLogin visao;
    private final UsuarioRepositorio usuarioRepositorio;
    private final CargoRepositorio cargoRepositorio;

    public LoginControlador(UsuarioRepositorio usuarioRepositorio, CargoRepositorio cargoRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.cargoRepositorio = cargoRepositorio;
    }

    public LoginControlador(TelaLogin visao, UsuarioRepositorio usuarioRepositorio, CargoRepositorio cargoRepositorio) {
        this(usuarioRepositorio, cargoRepositorio);
        this.visao = visao;
        initTela();
    }

    public void initTela() {
        visao.adicionarAcaoBtnEntrar(e -> entrar());
        visao.apresentarTela();
    }

    private void entrar() {
        String login = visao.getLogin();
        char[] senha = visao.getSenha();
        try {
            Usuario usuario = autenticar(login, senha);
            if (usuario.isDeveTrocarSenha()) {
                DialogoAlterarSenha dialogo = new DialogoAlterarSenha(visao, true);
                if (!new UsuarioControlador(usuarioRepositorio).alterarSenhaNaTela(dialogo, usuario)) {
                    sair();
                    visao.limparSenha();
                    visao.apresentarAviso("É preciso definir uma nova senha para entrar no sistema.");
                    return;
                }
            }
            visao.lembrarLogin(login);
            new TelaPrincipal(usuario).setVisible(true);
            visao.dispose();
        } catch (AthletizaException e) {
            visao.apresentarErro(e);
            visao.limparSenha();
        } finally {
            Arrays.fill(senha, '\0');
        }
    }

    public Usuario autenticar(String login, char[] senha)
            throws ValidacaoException, RegraNegocioException, PersistenciaException {
        new Validador()
                .obrigatorio(login, "Usuário")
                .regra(senha != null && senha.length > 0, "O campo \"Senha\" é obrigatório.")
                .validar();

        Usuario usuario = usuarioRepositorio.buscarPorLogin(login.trim())
                .filter(u -> Senha.conferir(senha, u.getSenhaHash()))
                .orElseThrow(() -> new RegraNegocioException(CREDENCIAIS_INVALIDAS));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Este usuário está desativado. Procure um administrador.");
        }
        Acesso acesso = new AcessoControlador(cargoRepositorio).calcular(usuario).orElseThrow(() -> new RegraNegocioException(
                "Seu usuário não está ligado a um cargo da gestão em andamento. Procure um administrador."));
        Sessao.iniciar(usuario, acesso);
        return usuario;
    }

    public void sair() {
        Sessao.encerrar();
    }
}
