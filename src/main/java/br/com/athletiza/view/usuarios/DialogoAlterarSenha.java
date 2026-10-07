package br.com.athletiza.view.usuarios;

import br.com.athletiza.controller.UsuarioController;
import br.com.athletiza.exception.AthletizaException;
import br.com.athletiza.model.Usuario;
import br.com.athletiza.view.componentes.Mensagens;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Troca da senha do usuário logado. Reabre enquanto houver erro, para o usuário corrigir.
 */
public final class DialogoAlterarSenha {

    private DialogoAlterarSenha() {
    }

    public static void abrir(Component pai, Usuario usuario) {
        JPasswordField atual = new JPasswordField(20);
        JPasswordField nova = new JPasswordField(20);
        JPasswordField confirmacao = new JPasswordField(20);
        JPanel campos = new JPanel(new GridLayout(0, 1, 0, 4));
        for (Object[] campo : new Object[][]{{"Senha atual", atual}, {"Nova senha (mínimo 6 caracteres)", nova},
            {"Confirmação da nova senha", confirmacao}}) {
            ((JPasswordField) campo[1]).putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
            campos.add(new JLabel((String) campo[0]));
            campos.add((Component) campo[1]);
        }

        UsuarioController controller = new UsuarioController();
        while (JOptionPane.showConfirmDialog(pai, campos, "Alterar minha senha", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            char[] senhaAtual = atual.getPassword();
            char[] senhaNova = nova.getPassword();
            char[] senhaConfirmacao = confirmacao.getPassword();
            try {
                controller.alterarSenha(usuario, senhaAtual, senhaNova, senhaConfirmacao);
                Mensagens.sucesso(pai, "Senha alterada com sucesso.");
                return;
            } catch (AthletizaException e) {
                Mensagens.erro(pai, e);
            } finally {
                Arrays.fill(senhaAtual, '\0');
                Arrays.fill(senhaNova, '\0');
                Arrays.fill(senhaConfirmacao, '\0');
            }
        }
    }
}
