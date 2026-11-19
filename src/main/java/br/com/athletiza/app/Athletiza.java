package br.com.athletiza.app;

import br.com.athletiza.controlador.LoginControlador;
import br.com.athletiza.dao.CargoDAO;
import br.com.athletiza.dao.UsuarioDAO;
import br.com.athletiza.util.Log;
import br.com.athletiza.visao.Tema;
import br.com.athletiza.visao.TelaLogin;
import br.com.athletiza.visao.componentes.Mensagens;
import javax.swing.SwingUtilities;

public class Athletiza {
    public static void main(String[] args) {
        Log.configurar();

        Thread.setDefaultUncaughtExceptionHandler((thread, erro) ->
                SwingUtilities.invokeLater(() -> Mensagens.erro(null, erro)));

        SwingUtilities.invokeLater(() -> {
            Tema.aplicar();
            TelaLogin visao = new TelaLogin();
            new LoginControlador(visao, new UsuarioDAO(), new CargoDAO());
        });
    }
}
