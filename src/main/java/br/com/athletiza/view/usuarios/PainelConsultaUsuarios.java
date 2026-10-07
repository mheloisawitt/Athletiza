package br.com.athletiza.view.usuarios;

import br.com.athletiza.controller.UsuarioController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.util.List;

/**
 * Consulta dos usuários do sistema (RF01). Disponível apenas para administradores (CH-13).
 */
public class PainelConsultaUsuarios extends PainelConsulta<Usuario> {

    private final UsuarioController controller = new UsuarioController();
    private final Navegador navegador;

    public PainelConsultaUsuarios(Navegador navegador) {
        super("Usuários", new ModeloTabela<Usuario>()
                .coluna("Nome", String.class, Usuario::getNome)
                .coluna("Login", String.class, Usuario::getLogin)
                .coluna("Perfil", String.class, u -> u.getPerfil().getDescricao())
                .coluna("Situação", Situacao.class, u -> u.isAtivo() ? Situacao.ATIVO : Situacao.INATIVO));
        this.navegador = navegador;
    }

    @Override
    protected List<Usuario> buscarDados() throws AthletizaException {
        return controller.listar();
    }

    @Override
    protected void incluir() {
        navegador.abrir(new PainelCadastroUsuario(new Usuario(), navegador));
    }

    @Override
    protected void editar(Usuario usuario) {
        navegador.abrir(new PainelCadastroUsuario(usuario, navegador));
    }

    @Override
    protected void excluir(Usuario usuario) {
        try {
            controller.excluir(usuario);
            Mensagens.sucesso(this, "Usuário excluído.");
        } catch (AthletizaException e) {
            Mensagens.erro(this, e);
        }
        carregar();
    }
}
