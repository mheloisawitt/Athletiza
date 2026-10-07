package br.com.athletiza.view.componentes;

import br.com.athletiza.exception.AthletizaException;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Ponto único para exibir mensagens ao usuário (CH-46).
 * Erros técnicos vão para o log; o usuário vê apenas uma mensagem compreensível.
 */
public final class Mensagens {

    private static final Logger LOG = Logger.getLogger(Mensagens.class.getName());

    private Mensagens() {
    }

    public static void erro(Component pai, Throwable erro) {
        if (erro instanceof AthletizaException) {
            JOptionPane.showMessageDialog(pai, erro.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } else {
            LOG.log(Level.SEVERE, "Erro inesperado", erro);
            JOptionPane.showMessageDialog(pai, "Ocorreu um erro inesperado. Tente novamente.\n"
                    + "Se o problema continuar, informe o responsável pelo sistema.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void aviso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public static void sucesso(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Pergunta Sim/Não. Retorna true se o usuário escolher Sim. */
    public static boolean confirmar(Component pai, String pergunta) {
        return JOptionPane.showConfirmDialog(pai, pergunta, "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
