package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Sessao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UsuarioControllerTest {

    private final UsuarioController controller = new UsuarioController();
    private final LoginController login = new LoginController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        Sessao.encerrar();
    }

    @Test
    void criaUsuarioLigadoAUmMembroQueConsegueEntrar() throws Exception {
        Usuario tesoureira = new Usuario("Paula Ribeiro", "paula");
        tesoureira.setMembro(membro("Paula Ribeiro"));

        controller.salvar(tesoureira, "segredo1".toCharArray(), "segredo1".toCharArray());

        login.autenticar("paula", "segredo1".toCharArray());
        assertEquals("Diretor · Financeiro", Sessao.getAcesso().getDescricao());
        assertEquals(6, controller.listar().size());
        assertEquals("Paula Ribeiro", controller.listar().stream().filter(u -> u.getLogin().equals("paula"))
                .findFirst().orElseThrow().getMembro().getNome());
    }

    @Test
    void validaLoginESenhaDoNovoUsuario() {
        Usuario invalido = new Usuario(" ", "ju liana");

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> controller.salvar(invalido, "123".toCharArray(), "124".toCharArray()));

        // nome, formato do login, membro (não é administrador), tamanho e confirmação da senha
        assertEquals(5, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void naoPermiteLoginRepetido() {
        Usuario repetido = new Usuario("Outro", "ADMIN");
        repetido.setAdministrador(true);

        assertThrows(RegraNegocioException.class,
                () -> controller.salvar(repetido, "segredo1".toCharArray(), "segredo1".toCharArray()));
    }

    @Test
    void alteracaoSemSenhaMantemASenhaAtual() throws Exception {
        Usuario admin = controller.listar().get(0);
        admin.setNome("Administrador Geral");

        controller.salvar(admin, new char[0], new char[0]);

        assertEquals("Administrador Geral", login.autenticar("admin", "admin123".toCharArray()).getNome());
    }

    @Test
    void sistemaNuncaFicaSemAdministradorAtivo() throws Exception {
        Usuario admin = controller.listar().get(0);

        admin.setAdministrador(false);
        admin.setMembro(membro("Paula Ribeiro"));
        assertThrows(RegraNegocioException.class, () -> controller.salvar(admin, null, null));
        admin.setAdministrador(true);
        admin.setAtivo(false);
        assertThrows(RegraNegocioException.class, () -> controller.salvar(admin, null, null));
        assertThrows(RegraNegocioException.class, () -> controller.excluir(admin));
    }

    @Test
    void naoExcluiOProprioUsuario() throws Exception {
        Usuario outroAdmin = new Usuario("Lucas Pereira", "lucas");
        outroAdmin.setAdministrador(true);
        controller.salvar(outroAdmin, "segredo1".toCharArray(), "segredo1".toCharArray());
        login.autenticar("lucas", "segredo1".toCharArray());

        assertThrows(RegraNegocioException.class, () -> controller.excluir(outroAdmin));
        controller.excluir(controller.listar().stream().filter(u -> u.getLogin().equals("admin")).findFirst().orElseThrow());
        assertEquals(5, controller.listar().size());
    }

    @Test
    void senhaDoAdministradorInicialEhProvisoriaAteSerTrocada() throws Exception {
        Usuario admin = login.autenticar("admin", "admin123".toCharArray());
        assertTrue(admin.isDeveTrocarSenha());

        controller.alterarSenha(admin, "admin123".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray());

        assertFalse(admin.isDeveTrocarSenha());
        assertFalse(login.autenticar("admin", "novaSenha".toCharArray()).isDeveTrocarSenha());
    }

    @Test
    void senhaDefinidaPeloAdministradorParaOutraPessoaEhProvisoria() throws Exception {
        login.autenticar("admin", "admin123".toCharArray());
        Usuario tesoureira = new Usuario("Paula Ribeiro", "paula");
        tesoureira.setMembro(membro("Paula Ribeiro"));

        controller.salvar(tesoureira, "segredo1".toCharArray(), "segredo1".toCharArray());
        assertTrue(login.autenticar("paula", "segredo1".toCharArray()).isDeveTrocarSenha());

        login.autenticar("admin", "admin123".toCharArray());
        Usuario admin = Sessao.getUsuarioLogado();
        controller.salvar(admin, "outraSenha".toCharArray(), "outraSenha".toCharArray());
        assertFalse(login.autenticar("admin", "outraSenha".toCharArray()).isDeveTrocarSenha());
    }

    @Test
    void trocaAPropriaSenhaConferindoASenhaAtual() throws Exception {
        Usuario admin = login.autenticar("admin", "admin123".toCharArray());

        assertThrows(RegraNegocioException.class, () -> controller.alterarSenha(admin,
                "errada".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray()));
        assertThrows(RegraNegocioException.class, () -> controller.alterarSenha(admin,
                "admin123".toCharArray(), "admin123".toCharArray(), "admin123".toCharArray()));
        controller.alterarSenha(admin, "admin123".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray());

        assertThrows(RegraNegocioException.class, () -> login.autenticar("admin", "admin123".toCharArray()));
        assertEquals("admin", login.autenticar("admin", "novaSenha".toCharArray()).getLogin());
    }

    private static Membro membro(String nome) throws Exception {
        return new MembroController().listar().stream().filter(m -> m.getNome().equals(nome)).findFirst().orElseThrow();
    }
}
