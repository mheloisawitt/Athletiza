package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Senha;
import br.com.athletiza.util.Sessao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginControllerTest {

    private final LoginController controller = new LoginController();

    @BeforeEach
    void prepararBanco() throws Exception {
        BancoDeTeste.recriar();
        Sessao.encerrar();
    }

    @Test
    void autenticaAdministradorDosDadosDeExemplo() throws Exception {
        Usuario usuario = controller.autenticar("ADMIN", "admin123".toCharArray());

        assertEquals("Administrador", usuario.getNome());
        assertEquals(Perfil.ADMINISTRADOR, usuario.getPerfil());
        assertEquals(usuario, Sessao.getUsuarioLogado());
    }

    @Test
    void senhaErradaELoginInexistenteTemAMesmaMensagem() {
        RegraNegocioException senhaErrada = assertThrows(RegraNegocioException.class,
                () -> controller.autenticar("admin", "errada".toCharArray()));
        RegraNegocioException loginInexistente = assertThrows(RegraNegocioException.class,
                () -> controller.autenticar("ninguem", "admin123".toCharArray()));

        assertEquals(LoginController.CREDENCIAIS_INVALIDAS, senhaErrada.getMessage());
        assertEquals(senhaErrada.getMessage(), loginInexistente.getMessage());
        assertNull(Sessao.getUsuarioLogado());
    }

    @Test
    void exigeUsuarioESenha() {
        ValidacaoException erro = assertThrows(ValidacaoException.class,
                () -> controller.autenticar(" ", new char[0]));

        assertEquals(2, erro.getErros().size());
    }

    @Test
    void bloqueiaUsuarioDesativado() throws Exception {
        Usuario consulta = new Usuario("Fulano", "fulano", Perfil.CONSULTA);
        consulta.setSenhaHash(Senha.gerarHash("123456".toCharArray()));
        consulta.setAtivo(false);
        new UsuarioDAO().inserir(consulta);

        assertThrows(RegraNegocioException.class, () -> controller.autenticar("fulano", "123456".toCharArray()));
    }
}
