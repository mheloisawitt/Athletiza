package br.com.athletiza.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletiza.BancoDeTeste;
import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.exception.RegraNegocioException;
import br.com.athletiza.exception.ValidacaoException;
import br.com.athletiza.model.Membro;
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
        assertEquals(Perfil.ADMINISTRADOR, Sessao.getAcesso().getPerfil());
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
        Usuario consulta = new Usuario("Fulano", "fulano");
        consulta.setAdministrador(true);
        consulta.setSenhaHash(Senha.gerarHash("123456".toCharArray()));
        consulta.setAtivo(false);
        new UsuarioDAO().inserir(consulta);

        assertThrows(RegraNegocioException.class, () -> controller.autenticar("fulano", "123456".toCharArray()));
    }

    @Test
    void perfilVemDoCargoNaGestaoEmAndamento() throws Exception {
        assertEquals("Presidência", acessoDe("mariana"));
        assertEquals("Diretor · Esportes", acessoDe("juliana"));
        assertEquals("Diretor · Eventos", acessoDe("rafael"));
        assertEquals("Assessor · Esportes", acessoDe("bruno"));
    }

    @Test
    void membroSemCargoNaGestaoEmAndamentoNaoEntra() throws Exception {
        Membro semCargo = new Membro("Carlos Souza", "202199", null);
        new MembroController().salvar(semCargo);
        Usuario usuario = new Usuario("Carlos Souza", "carlos");
        usuario.setMembro(semCargo);
        usuario.setSenhaHash(Senha.gerarHash("123456".toCharArray()));
        new UsuarioDAO().inserir(usuario);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> controller.autenticar("carlos", "123456".toCharArray()));
        assertTrue(erro.getMessage().contains("gestão em andamento"), erro.getMessage());
        assertNull(Sessao.getUsuarioLogado());
    }

    private String acessoDe(String login) throws Exception {
        controller.autenticar(login, "athletiza".toCharArray());
        return Sessao.getAcesso().getDescricao();
    }
}
