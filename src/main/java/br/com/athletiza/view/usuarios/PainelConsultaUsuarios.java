package br.com.athletiza.view.usuarios;

import br.com.athletiza.controller.AcessoController;
import br.com.athletiza.controller.UsuarioController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Sessao;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.ModeloTabela;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelConsulta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consulta dos usuários do sistema (RF01). Disponível apenas para administradores (CH-13).
 */
public class PainelConsultaUsuarios extends PainelConsulta<Usuario> {

    private final UsuarioController controller = new UsuarioController();
    private final Navegador navegador;
    /** Acesso de cada usuário, calculado pelo cargo na gestão em andamento a cada carregamento. */
    private final Map<Usuario, String> acessos;

    public PainelConsultaUsuarios(Navegador navegador) {
        this(navegador, new HashMap<>());
    }

    private PainelConsultaUsuarios(Navegador navegador, Map<Usuario, String> acessos) {
        super("Usuários", new ModeloTabela<Usuario>()
                .coluna("Nome", String.class, Usuario::getNome)
                .coluna("Login", String.class, Usuario::getLogin)
                .coluna("Membro", String.class, u -> u.getMembro() == null ? "" : u.getMembro().getNome())
                .coluna("Acesso", String.class, u -> acessos.getOrDefault(u, ""))
                .coluna("Situação", Situacao.class, u -> u.isAtivo() ? Situacao.ATIVO : Situacao.INATIVO));
        this.navegador = navegador;
        this.acessos = acessos;
        definirPermissao(() -> Sessao.getAcesso().podeGerenciarUsuarios());
    }

    @Override
    protected List<Usuario> buscarDados() throws AthletizaException {
        List<Usuario> usuarios = controller.listar();
        AcessoController acesso = new AcessoController();
        acessos.clear();
        for (Usuario usuario : usuarios) {
            acessos.put(usuario, acesso.descrever(usuario));
        }
        return usuarios;
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
