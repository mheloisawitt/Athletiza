package br.com.athletiza;

import br.com.athletiza.util.Cores;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do sistema Athletiza.
 *
 * Por enquanto abre apenas uma janela inicial para confirmar que o projeto
 * compila e executa. Será substituída pela tela de login (CH-12).
 */
public class Athletiza {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Athletiza - Sistema de Gerenciamento");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.getContentPane().setBackground(Cores.FUNDO);
            janela.setLayout(new BorderLayout());

            JLabel titulo = new JLabel("ATHLETIZA", SwingConstants.CENTER);
            titulo.setFont(new Font("SansSerif", Font.BOLD, 36));
            titulo.setForeground(Cores.VERDE);
            janela.add(titulo, BorderLayout.CENTER);

            janela.setSize(800, 500);
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
        });
    }
}
