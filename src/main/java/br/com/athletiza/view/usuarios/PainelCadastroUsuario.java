package br.com.athletiza.view.usuarios;

import br.com.athletiza.controller.AcessoController;
import br.com.athletiza.controller.MembroController;
import br.com.athletiza.controller.UsuarioController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.exception.PersistenciaException;
import br.com.athletiza.model.Membro;
import br.com.athletiza.model.Situacao;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.util.Cores;
import br.com.athletiza.view.componentes.Combos;
import br.com.athletiza.view.componentes.Mensagens;
import br.com.athletiza.view.componentes.Navegador;
import br.com.athletiza.view.componentes.PainelFormulario;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Cadastro e edição de usuário (RF01, RN19). O administrador libera o acesso ligando o
 * login a um membro: o perfil (Presidência, Diretor ou Assessor) vem do cargo dele na
 * gestão em andamento. Na edição, a senha só é trocada se for preenchida.
 */
public class PainelCadastroUsuario extends PainelFormulario {

    private final Usuario usuario;
    private final UsuarioController controller = new UsuarioController();
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
        this.usuario = usuario;

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

        carregarMembros();
        nome.setText(usuario.getNome());
        login.setText(usuario.getLogin());
        membro.setSelectedItem(usuario.getMembro());
        administrador.setSelected(usuario.isAdministrador());
        situacao.setSelectedItem(usuario.isAtivo() ? Situacao.ATIVO : Situacao.INATIVO);

        membro.addActionListener(e -> aoEscolherMembro());
        administrador.addActionListener(e -> mostrarAcesso());
        mostrarAcesso();
    }

    /** Membros ativos, mais o já ligado ao usuário (mesmo que tenha ficado inativo). */
    private void carregarMembros() {
        try {
            List<Membro> membros = new ArrayList<>(new MembroController().listarAtivos());
            if (usuario.getMembro() != null && !membros.contains(usuario.getMembro())) {
                membros.add(usuario.getMembro());
            }
            Combos.atualizarItens(membro, membros);
        } catch (PersistenciaException e) {
            Mensagens.erro(this, e);
        }
    }

    /** Preenche o nome com o do membro escolhido, se ainda estiver vazio. */
    private void aoEscolherMembro() {
        Membro escolhido = (Membro) membro.getSelectedItem();
        if (escolhido != null && nome.getText().isBlank()) {
            nome.setText(escolhido.getNome());
        }
        mostrarAcesso();
    }

    /** Mostra o acesso que o usuário terá com o membro e a marcação escolhidos. */
    private void mostrarAcesso() {
        Usuario previa = new Usuario();
        previa.setAdministrador(administrador.isSelected());
        previa.setMembro((Membro) membro.getSelectedItem());
        try {
            String descricao = previa.isAdministrador() || previa.getMembro() != null
                    ? new AcessoController().descrever(previa) : "escolha o membro da gestão";
            acesso.setText("Acesso: " + descricao);
        } catch (PersistenciaException e) {
            acesso.setText(" ");
        }
    }

    @Override
    protected void salvar() throws AthletizaException {
        char[] novaSenha = senha.getPassword();
        char[] novaConfirmacao = confirmacao.getPassword();
        try {
            Membro escolhido = (Membro) membro.getSelectedItem();
            usuario.setNome(nome.getText().trim());
            usuario.setLogin(login.getText().trim());
            usuario.setMembro(escolhido);
            usuario.setMatricula(escolhido == null ? usuario.getMatricula() : escolhido.getMatricula());
            usuario.setAdministrador(administrador.isSelected());
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
