package br.com.athletiza.controlador;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.dao.MembroDAO;
import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.excecao.RegraNegocioException;
import br.com.athletiza.excecao.ValidacaoException;
import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.util.Sessao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioControladorTest {
    private final UsuarioControlador controlador = new UsuarioControlador(new UsuarioDAO());
    private final LoginControlador login = new LoginControlador(new UsuarioDAO(), new CargoDAO());

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        Sessao.encerrar();
    }

    @Test
    void criaUsuarioLigadoAUmMembroQueConsegueEntrar() throws Exception {
        Usuario tesoureira = new Usuario("Paula Ribeiro", "paula");
        tesoureira.setMembro(membro("Paula Ribeiro"));

        controlador.salvar(tesoureira, "segredo1".toCharArray(), "segredo1".toCharArray());

        login.autenticar("paula", "segredo1".toCharArray());
        assertEquals("Diretor · Financeiro", Sessao.getAcesso().getDescricao());
        assertEquals(6, controlador.listar().size());
        assertEquals("Paula Ribeiro", controlador.listar().stream().filter(u -> u.getLogin().equals("paula"))
                .findFirst().orElseThrow().getMembro().getNome());
    }

    @Test
    void validaLoginESenhaDoNovoUsuario() {
        Usuario invalido = new Usuario(" ", "ju liana");

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> controlador.salvar(invalido, "123".toCharArray(), "124".toCharArray()));

        assertEquals(5, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void naoPermiteLoginRepetido() {
        Usuario repetido = new Usuario("Outro", "ADMIN");
        repetido.setAdministrador(true);

        assertThrows(RegraNegocioException.class,
                () -> controlador.salvar(repetido, "segredo1".toCharArray(), "segredo1".toCharArray()));
    }

    @Test
    void alteracaoSemSenhaMantemASenhaAtual() throws Exception {
        Usuario admin = controlador.listar().get(0);
        admin.setNome("Administrador Geral");

        controlador.salvar(admin, new char[0], new char[0]);

        assertEquals("Administrador Geral", login.autenticar("admin", "admin123".toCharArray()).getNome());
    }

    @Test
    void sistemaNuncaFicaSemAdministradorAtivo() throws Exception {
        Usuario admin = controlador.listar().get(0);

        admin.setAdministrador(false);
        admin.setMembro(membro("Paula Ribeiro"));
        assertThrows(RegraNegocioException.class, () -> controlador.salvar(admin, null, null));
        admin.setAdministrador(true);
        admin.setAtivo(false);
        assertThrows(RegraNegocioException.class, () -> controlador.salvar(admin, null, null));
        assertThrows(RegraNegocioException.class, () -> controlador.excluir(admin));
    }

    @Test
    void naoExcluiOProprioUsuario() throws Exception {
        Usuario outroAdmin = new Usuario("Lucas Pereira", "lucas");
        outroAdmin.setAdministrador(true);
        controlador.salvar(outroAdmin, "segredo1".toCharArray(), "segredo1".toCharArray());
        login.autenticar("lucas", "segredo1".toCharArray());

        assertThrows(RegraNegocioException.class, () -> controlador.excluir(outroAdmin));
        controlador.excluir(controlador.listar().stream().filter(u -> u.getLogin().equals("admin")).findFirst().orElseThrow());
        assertEquals(5, controlador.listar().size());
    }

    @Test
    void senhaDoAdministradorInicialEhProvisoriaAteSerTrocada() throws Exception {
        Usuario admin = login.autenticar("admin", "admin123".toCharArray());
        assertTrue(admin.isDeveTrocarSenha());

        controlador.alterarSenha(admin, "admin123".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray());

        assertFalse(admin.isDeveTrocarSenha());
        assertFalse(login.autenticar("admin", "novaSenha".toCharArray()).isDeveTrocarSenha());
    }

    @Test
    void senhaDefinidaPeloAdministradorParaOutraPessoaEhProvisoria() throws Exception {
        login.autenticar("admin", "admin123".toCharArray());
        Usuario tesoureira = new Usuario("Paula Ribeiro", "paula");
        tesoureira.setMembro(membro("Paula Ribeiro"));

        controlador.salvar(tesoureira, "segredo1".toCharArray(), "segredo1".toCharArray());
        assertTrue(login.autenticar("paula", "segredo1".toCharArray()).isDeveTrocarSenha());

        login.autenticar("admin", "admin123".toCharArray());
        Usuario admin = Sessao.getUsuarioLogado();
        controlador.salvar(admin, "outraSenha".toCharArray(), "outraSenha".toCharArray());
        assertFalse(login.autenticar("admin", "outraSenha".toCharArray()).isDeveTrocarSenha());
    }

    @Test
    void trocaAPropriaSenhaConferindoASenhaAtual() throws Exception {
        Usuario admin = login.autenticar("admin", "admin123".toCharArray());

        assertThrows(RegraNegocioException.class, () -> controlador.alterarSenha(admin,
                "errada".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray()));
        assertThrows(RegraNegocioException.class, () -> controlador.alterarSenha(admin,
                "admin123".toCharArray(), "admin123".toCharArray(), "admin123".toCharArray()));
        controlador.alterarSenha(admin, "admin123".toCharArray(), "novaSenha".toCharArray(), "novaSenha".toCharArray());

        assertThrows(RegraNegocioException.class, () -> login.autenticar("admin", "admin123".toCharArray()));
        assertEquals("admin", login.autenticar("admin", "novaSenha".toCharArray()).getLogin());
    }

    private static Membro membro(String nome) throws Exception {
        return new MembroControlador(new MembroDAO()).listar().stream().filter(m -> m.getNome().equals(nome)).findFirst().orElseThrow();
    }
}
