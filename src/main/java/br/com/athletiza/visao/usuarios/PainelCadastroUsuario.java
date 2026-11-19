package br.com.athletiza.visao.usuarios;

import br.com.athletiza.modelo.Membro;
import br.com.athletiza.modelo.Situacao;
import br.com.athletiza.modelo.Usuario;
import br.com.athletiza.util.Cores;
import br.com.athletiza.visao.componentes.Combos;
import br.com.athletiza.visao.componentes.Navegador;
import br.com.athletiza.visao.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class PainelCadastroUsuario extends PainelFormulario {
    private final JTextField nome = new JTextField();
    private final JTextField login = new JTextField();
    private final JComboBox<Membro> membro = Combos.comOpcaoTodos(List.of(), "Nenhum");
    private final JCheckBox administrador = new JCheckBox("Administrador (acesso total, inclusive usuários)");
    private final JComboBox<Situacao> situacao = new JComboBox<>(Situacao.values());
    private final JPasswordField senha = new JPasswordField();
    private final JPasswordField confirmacao = new JPasswordField();
    private final JLabel acesso = new JLabel(" ");

    public PainelCadastroUsuario(Usuario usuario, Navegador navegador) {
        super(usuario.isNova() ? "Novo Usuário" : "Editar Usuário", navegador);

        nome.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome completo");
        login.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ex: juliana.martins");
        for (JPasswordField campo : new JPasswordField[]{senha, confirmacao}) {
            campo.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
            campo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                    usuario.isNova() ? "Mínimo de 6 caracteres" : "Deixe em branco para manter a atual");
        }
        administrador.setOpaque(false);
        acesso.setFont(acesso.getFont().deriveFont(Font.BOLD));

        adicionarCampo("Membro da gestão", membro, false);
        adicionarCampo("Nome", nome, true);
        adicionarCampo("Login", login, true);
        adicionarCampo("Situação", situacao, true);
        adicionarCampo(usuario.isNova() ? "Senha" : "Nova senha", senha, usuario.isNova());
        adicionarCampo("Confirmação da senha", confirmacao, usuario.isNova());
        adicionarLinha(administrador);
        adicionarLinha(acesso);

        JLabel ajuda = new JLabel("<html><body style='width: 640px'>O acesso vem do cargo do membro na gestão em andamento: Presidente ou Vice"
                + " → Presidência (altera tudo, menos usuários); diretor de um setor → consulta tudo e altera só a"
                + " área da sua diretoria; cargo subordinado a um diretor do mesmo setor → Assessor (só consulta"
                + " o seu setor). O setor de cada cargo é definido em Gestão → Cargos.<br>A senha definida aqui"
                + " para outra pessoa é provisória: ela deverá trocá-la no primeiro acesso.</html>");
        ajuda.setForeground(Cores.TEXTO_SECUNDARIO);
        adicionarLinha(ajuda);

        membro.addActionListener(e -> {
            Membro escolhido = (Membro) membro.getSelectedItem();
            if (escolhido != null && nome.getText().isBlank()) {
                nome.setText(escolhido.getNome());
            }
        });
    }

    public void apresentarUsuario(Usuario usuario, List<Membro> membros) {
        Combos.atualizarItens(membro, membros);
        nome.setText(usuario.getNome());
        login.setText(usuario.getLogin());
        membro.setSelectedItem(usuario.getMembro());
        administrador.setSelected(usuario.isAdministrador());
        situacao.setSelectedItem(usuario.isAtivo() ? Situacao.ATIVO : Situacao.INATIVO);
    }

    public void adicionarAcaoMudarAcesso(ActionListener acao) {
        membro.addActionListener(acao);
        administrador.addActionListener(acao);
    }

    public void apresentarAcesso(String descricao) {
        acesso.setText("Acesso: " + descricao);
    }

    public String getNome() {
        return nome.getText().trim();
    }

    public String getLogin() {
        return login.getText().trim();
    }

    public Membro getMembro() {
        return (Membro) membro.getSelectedItem();
    }

    public boolean isAdministrador() {
        return administrador.isSelected();
    }

    public boolean isAtivo() {
        return situacao.getSelectedItem() == Situacao.ATIVO;
    }

    public char[] getSenha() {
        return senha.getPassword();
    }

    public char[] getConfirmacao() {
        return confirmacao.getPassword();
    }
}
