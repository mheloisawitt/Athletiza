package br.com.athletiza.view.usuarios;

import br.com.athletiza.controller.UsuarioController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.Arrays;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Cadastro e edição de usuário (RF01). Na edição, a senha só é trocada se for preenchida.
 */
public class PainelCadastroUsuario extends PainelFormulario {

    private final Usuario usuario;
    private final UsuarioController controller = new UsuarioController();
    private final JTextField nome = new JTextField();
    private final JTextField login = new JTextField();
    private final JComboBox<Perfil> perfil = new JComboBox<>(Perfil.values());
    private final JComboBox<Situacao> situacao = new JComboBox<>(Situacao.values());
    private final JPasswordField senha = new JPasswordField();
    private final JPasswordField confirmacao = new JPasswordField();

    public PainelCadastroUsuario(Usuario usuario, Navegador navegador) {
        super(usuario.isNova() ? "Novo Usuário" : "Editar Usuário", navegador);
        this.usuario = usuario;

        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome completo");
        login.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: juliana.martins");
        for (JPasswordField campo : new JPasswordField[]{senha, confirmacao}) {
            campo.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
            campo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                    usuario.isNova() ? "Mínimo de 6 caracteres" : "Deixe em branco para manter a atual");
        }

        adicionarCampo("Nome", nome, true);
        adicionarCampo("Login", login, true);
        adicionarCampo("Perfil", perfil, true);
        adicionarCampo("Situação", situacao, true);
        adicionarCampo(usuario.isNova() ? "Senha" : "Nova senha", senha, usuario.isNova());
        adicionarCampo("Confirmação da senha", confirmacao, usuario.isNova());

        JLabel ajuda = new JLabel("<html>Administrador: acesso total, inclusive usuários. Diretoria: inclui, edita e exclui"
                + " registros. Consulta: apenas visualiza.<br>A senha definida aqui para outra pessoa é provisória:"
                + " ela deverá trocá-la no primeiro acesso.</html>");
        ajuda.setForeground(Cores.TEXTO_SECUNDARIO);
        adicionarLinha(ajuda);

        nome.setText(usuario.getNome());
        login.setText(usuario.getLogin());
        perfil.setSelectedItem(usuario.getPerfil());
        situacao.setSelectedItem(usuario.isAtivo() ? Situacao.ATIVO : Situacao.INATIVO);
    }

    @Override
    protected void salvar() throws AthletizaException {
        char[] novaSenha = senha.getPassword();
        char[] novaConfirmacao = confirmacao.getPassword();
        try {
            usuario.setNome(nome.getText().trim());
            usuario.setLogin(login.getText().trim());
            usuario.setPerfil((Perfil) perfil.getSelectedItem());
            usuario.setAtivo(situacao.getSelectedItem() == Situacao.ATIVO);
            controller.salvar(usuario, novaSenha, novaConfirmacao);
        } finally {
            Arrays.fill(novaSenha, '\0');
            Arrays.fill(novaConfirmacao, '\0');
        }
    }

    @Override
    protected String getMensagemSucesso() {
        return "Usuário salvo com sucesso.";
    }
}
