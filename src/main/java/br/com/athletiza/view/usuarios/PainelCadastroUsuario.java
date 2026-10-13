package br.com.athletiza.view.usuarios;

import br.com.athletiza.model.Perfil;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Cadastro e edição de usuário (RF01). Na edição, a senha só é trocada se for preenchida.
 * As ações ficam no CadastroUsuarioController.
 */
public class PainelCadastroUsuario extends PainelFormulario {

    private final JTextField nome = new JTextField();
    private final JTextField login = new JTextField();
    private final JComboBox<Perfil> perfil = new JComboBox<>(Perfil.values());
    private final JComboBox<Situacao> situacao = new JComboBox<>(Situacao.values());
    private final JPasswordField senha = new JPasswordField();
    private final JPasswordField confirmacao = new JPasswordField();
    private final boolean novo;

    /** @param novo true na inclusão (a senha é obrigatória) */
    public PainelCadastroUsuario(boolean novo) {
        this.novo = novo;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome completo");
        login.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: juliana.martins");
        for (JPasswordField campo : new JPasswordField[]{senha, confirmacao}) {
            campo.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
            campo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                    novo ? "Mínimo de 6 caracteres" : "Deixe em branco para manter a atual");
        }

        adicionarCampo("Nome", nome, true);
        adicionarCampo("Login", login, true);
        adicionarCampo("Perfil", perfil, true);
        adicionarCampo("Situação", situacao, true);
        adicionarCampo(novo ? "Senha" : "Nova senha", senha, novo);
        adicionarCampo("Confirmação da senha", confirmacao, novo);

        JLabel ajuda = new JLabel("<html>Administrador: acesso total, inclusive usuários. Diretoria: inclui, edita e exclui"
                + " registros. Consulta: apenas visualiza.<br>A senha definida aqui para outra pessoa é provisória:"
                + " ela deverá trocá-la no primeiro acesso.</html>");
        ajuda.setForeground(Cores.TEXTO_SECUNDARIO);
        adicionarLinha(ajuda);
    }

    public void preencherCampos(Usuario usuario) {
        setTitulo(usuario.isNova() ? "Novo Usuário" : "Editar Usuário");
        nome.setText(usuario.getNome());
        login.setText(usuario.getLogin());
        perfil.setSelectedItem(usuario.getPerfil());
        situacao.setSelectedItem(usuario.isAtivo() ? Situacao.ATIVO : Situacao.INATIVO);
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public String getLogin() {
        return login.getText().trim();
    }

    public Perfil getPerfil() {
        return (Perfil) perfil.getSelectedItem();
    }

    public boolean isAtivo() {
        return situacao.getSelectedItem() == Situacao.ATIVO;
    }

    /** Senha digitada. Quem chama deve apagar o array depois de usar (Arrays.fill). */
    public char[] getSenha() {
        return senha.getPassword();
    }

    public char[] getConfirmacao() {
        return confirmacao.getPassword();
    }

    /** Preenche os campos de senha (usado nos testes). */
    public void setSenha(String novaSenha, String novaConfirmacao) {
        senha.setText(novaSenha);
        confirmacao.setText(novaConfirmacao);
    }
}
