package br.com.athletiza.visao.usuarios;

import br.com.athletiza.visao.componentes.Mensagens;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class DialogoAlterarSenha {
    private final Component pai;
    private final String titulo;
    private final JPasswordField atual = new JPasswordField(20);
    private final JPasswordField nova = new JPasswordField(20);
    private final JPasswordField confirmacao = new JPasswordField(20);
    private final JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));

    public DialogoAlterarSenha(Component pai, boolean obrigatorio) {
        this.pai = pai;
        this.titulo = obrigatorio ? "Defina uma nova senha" : "Alterar minha senha";
        if (obrigatorio) {
            campos.add(new JLabel("<html><b>Sua senha é provisória.</b><br>"
                    + "Por segurança, defina uma nova senha para continuar.</html>"));
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

    public boolean apresentarTela() {
        return JOptionPane.showConfirmDialog(pai, campos, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    public char[] getSenhaAtual() {
        return atual.getPassword();
    }

    public char[] getNovaSenha() {
        return nova.getPassword();
    }

    public char[] getConfirmacao() {
        return confirmacao.getPassword();
    }

    public void apresentarMensagem(String mensagem) {
        Mensagens.sucesso(pai, mensagem);
    }

    public void apresentarErro(Exception erro) {
        Mensagens.erro(pai, erro);
    }
}
