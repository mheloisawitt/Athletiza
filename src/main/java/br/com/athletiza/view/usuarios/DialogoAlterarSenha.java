package br.com.athletiza.view.usuarios;

import br.com.athletiza.view.componentes.Mensagens;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Troca da senha do usuário logado. O AlterarSenhaController reabre o diálogo
 * enquanto houver erro, para o usuário corrigir.
 */
public class DialogoAlterarSenha {

    private final Component pai;
    private final JPasswordField atual = new JPasswordField(20);
    private final JPasswordField nova = new JPasswordField(20);
    private final JPasswordField confirmacao = new JPasswordField(20);
    private final JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));

    /**
     * @param aviso texto exibido acima dos campos (ex.: na troca obrigatória), ou null
     */
    public DialogoAlterarSenha(Component pai, String aviso) {
        this.pai = pai;
        inicializarComponentes(aviso);
    }

    private void inicializarComponentes(String aviso) {
        if (aviso != null) {
            campos.add(new JLabel(aviso));
        }
        adicionar("Senha atual", atual);
        adicionar("Nova senha (mínimo 6 caracteres)", nova);
        adicionar("Confirmação da nova senha", confirmacao);
    }

    private void adicionar(String rotulo, JPasswordField campo) {
        campo.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        campos.add(new JLabel(rotulo));
        campos.add(campo);
    }

    /**
     * Mostra o diálogo.
     *
     * @return true se o usuário clicou em OK
     */
    public boolean exibir(String titulo) {
        return JOptionPane.showConfirmDialog(pai, campos, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    /** Senhas digitadas. Quem chama deve apagar os arrays depois de usar (Arrays.fill). */
    public char[] getSenhaAtual() {
        return atual.getPassword();
    }

    public char[] getNovaSenha() {
        return nova.getPassword();
    }

    public char[] getConfirmacao() {
        return confirmacao.getPassword();
    }

    public void mostrarMensagem(String mensagem) {
        Mensagens.sucesso(pai, mensagem);
    }

    public void mostrarErro(Exception erro) {
        Mensagens.erro(pai, erro);
    }
}
