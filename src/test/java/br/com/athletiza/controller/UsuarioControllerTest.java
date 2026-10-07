package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Perfil;
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
    void criaUsuarioQueConsegueEntrar() throws Exception {
        Usuario diretora = new Usuario("Juliana Martins", "juliana", Perfil.DIRETORIA);

        controller.salvar(diretora, "segredo1".toCharArray(), "segredo1".toCharArray());

        assertEquals(Perfil.DIRETORIA, login.autenticar("juliana", "segredo1".toCharArray()).getPerfil());
        assertEquals(2, controller.listar().size());
    }

    @Test
    void validaLoginESenhaDoNovoUsuario() {
        Usuario invalido = new Usuario(" ", "ju liana", Perfil.CONSULTA);

        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> controller.salvar(invalido, "123".toCharArray(), "124".toCharArray()));

        assertEquals(4, erro.getErros().size(), erro.getMessage());
    }

    @Test
    void naoPermiteLoginRepetido() {
        Usuario repetido = new Usuario("Outro", "ADMIN", Perfil.CONSULTA);

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

        admin.setPerfil(Perfil.DIRETORIA);
        assertThrows(RegraNegocioException.class, () -> controller.salvar(admin, null, null));
        admin.setPerfil(Perfil.ADMINISTRADOR);
        admin.setAtivo(false);
        assertThrows(RegraNegocioException.class, () -> controller.salvar(admin, null, null));
        assertThrows(RegraNegocioException.class, () -> controller.excluir(admin));
    }

    @Test
    void naoExcluiOProprioUsuario() throws Exception {
        Usuario outroAdmin = new Usuario("Mariana", "mariana", Perfil.ADMINISTRADOR);
        controller.salvar(outroAdmin, "segredo1".toCharArray(), "segredo1".toCharArray());
        Sessao.iniciar(outroAdmin);

        assertThrows(RegraNegocioException.class, () -> controller.excluir(outroAdmin));
        controller.excluir(controller.listar().stream().filter(u -> u.getLogin().equals("admin")).findFirst().orElseThrow());
        assertEquals(1, controller.listar().size());
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
}
