package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.NavegadorFalso;
import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.usuarios.DialogoAlterarSenha;
import br.com.athletiza.view.usuarios.PainelCadastroUsuario;
import br.com.athletiza.view.usuarios.PainelConsultaUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsuarioControllerTest {

    private final NavegadorFalso navegador = new NavegadorFalso();
    private final UsuarioDAO dao = new UsuarioDAO();
    private final LoginController login = new LoginController(null, dao);
    private final ConsultaUsuariosController consulta =
            new ConsultaUsuariosController(new PainelConsultaUsuarios(), dao, navegador);

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        Sessao.encerrar();
    }

    /** Grava o usuário como se ele tivesse sido preenchido na tela de cadastro. */
    private void salvar(Usuario usuario, String senha, String confirmacao) throws Exception {
        CadastroUsuarioController cadastro = new CadastroUsuarioController(new PainelCadastroUsuario(usuario.isNova()),
                usuario, dao, navegador);
        cadastro.gravar(senha == null ? null : senha.toCharArray(), confirmacao == null ? null : confirmacao.toCharArray());
    }

    private AlterarSenhaController alterarSenha(Usuario usuario) {
        return new AlterarSenhaController(new DialogoAlterarSenha(null, null), usuario, dao);
    }

    private Usuario admin() throws Exception {
        return dao.buscarPorLogin("admin");
    }

    @Test
    void criaUsuarioQueConsegueEntrar() throws Exception {
        Usuario diretora = new Usuario("Juliana Martins", "juliana", Perfil.DIRETORIA);

        salvar(diretora, "segredo1", "segredo1");

        assertEquals(Perfil.DIRETORIA, login.autenticar("juliana", "segredo1".toCharArray()).getPerfil());
        assertEquals(2, dao.listarTodos().size());
    }

    @Test
    void validaLoginESenhaDoNovoUsuario() {
        Usuario invalido = new Usuario(" ", "ju liana", Perfil.CONSULTA);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> salvar(invalido, "123", "124"));

        assertEquals(4, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void naoPermiteLoginRepetido() {
        Usuario repetido = new Usuario("Outro", "ADMIN", Perfil.CONSULTA);

        assertThrows(RegraNegocioException.class, () -> salvar(repetido, "segredo1", "segredo1"));
    }

    @Test
    void alteracaoSemSenhaMantemASenhaAtual() throws Exception {
        Usuario admin = admin();
        admin.setNome("Administrador Geral");

        salvar(admin, "", "");

        assertEquals("Administrador Geral", login.autenticar("admin", "admin123".toCharArray()).getNome());
    }

    @Test
    void sistemaNuncaFicaSemAdministradorAtivo() throws Exception {
        Usuario admin = admin();

        admin.setPerfil(Perfil.DIRETORIA);
        assertThrows(RegraNegocioException.class, () -> salvar(admin, null, null));
        admin.setPerfil(Perfil.ADMINISTRADOR);
        admin.setAtivo(false);
        assertThrows(RegraNegocioException.class, () -> salvar(admin, null, null));
        assertThrows(RegraNegocioException.class, () -> consulta.excluir(admin));
    }

    @Test
    void naoExcluiOProprioUsuario() throws Exception {
        Usuario outroAdmin = new Usuario("Mariana", "mariana", Perfil.ADMINISTRADOR);
        salvar(outroAdmin, "segredo1", "segredo1");
        Sessao.iniciar(outroAdmin);

        assertThrows(RegraNegocioException.class, () -> consulta.excluir(outroAdmin));
        consulta.excluir(admin());
        assertEquals(1, dao.listarTodos().size());
    }

    @Test
    void senhaDoAdministradorInicialEhProvisoriaAteSerTrocada() throws Exception {
        Usuario admin = login.autenticar("admin", "admin123".toCharArray());
        assertTrue(admin.isDeveTrocarSenha());

        alterarSenha(admin).alterarSenha("admin123".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray());

        assertFalse(admin.isDeveTrocarSenha());
        assertFalse(login.autenticar("admin", "novaSenha".toCharArray()).isDeveTrocarSenha());
    }

    @Test
    void senhaDefinidaPeloAdministradorParaOutraPessoaEhProvisoria() throws Exception {
        Sessao.iniciar(login.autenticar("admin", "admin123".toCharArray()));
        Usuario diretora = new Usuario("Juliana Martins", "juliana", Perfil.DIRETORIA);

        salvar(diretora, "segredo1", "segredo1");
        assertTrue(login.autenticar("juliana", "segredo1".toCharArray()).isDeveTrocarSenha());

        Usuario admin = admin();
        Sessao.iniciar(admin);
        salvar(admin, "outraSenha", "outraSenha");
        assertFalse(login.autenticar("admin", "outraSenha".toCharArray()).isDeveTrocarSenha());
    }

    @Test
    void trocaAPropriaSenhaConferindoASenhaAtual() throws Exception {
        Usuario admin = login.autenticar("admin", "admin123".toCharArray());
        AlterarSenhaController controller = alterarSenha(admin);

        assertThrows(RegraNegocioException.class, () -> controller.alterarSenha(
                "errada".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray()));
        assertThrows(RegraNegocioException.class, () -> controller.alterarSenha(
                "admin123".toCharArray(), "admin123".toCharArray(), "admin123".toCharArray()));
        controller.alterarSenha("admin123".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray());

        assertThrows(RegraNegocioException.class, () -> login.autenticar("admin", "admin123".toCharArray()));
        assertEquals("admin", login.autenticar("admin", "novaSenha".toCharArray()).getLogin());
    }
}
