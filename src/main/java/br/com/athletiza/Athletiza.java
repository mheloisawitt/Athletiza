package br.com.athletiza;

import br.com.athletiza.util.Log;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.TelaLogin;
import br.com.athletiza.view.componentes.Mensagens;
import javax.swing.SwingUtilities;

/**
 * Ponto de entrada do sistema Athletiza.
 */
public class Athletiza {

    public static void main(String[] args) {
        Log.configurar();
        // Qualquer erro não tratado vira uma mensagem amigável em vez de fechar o sistema (CH-46)
        Thread.setDefaultUncaughtExceptionHandler((thread, erro) ->
                SwingUtilities.invokeLater(() -> Mensagens.erro(null, erro)));

        SwingUtilities.invokeLater(() -> {
            Tema.aplicar();
            new TelaLogin().setVisible(true);
        });
    }
}
