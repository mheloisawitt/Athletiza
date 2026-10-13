package br.com.athletiza.view.calendario;

import br.com.athletiza.view.componentes.Navegador;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * Diálogo de inclusão e edição de compromissos a partir do calendário (CH-16).
 * Mostra o PainelCompromisso; como é um Navegador, "voltar" fecha o diálogo.
 */
public class DialogoCompromisso extends JDialog implements Navegador {

    private final PainelCompromisso formulario = new PainelCompromisso();

    public DialogoCompromisso(Component pai, String titulo) {
        super(SwingUtilities.getWindowAncestor(pai), titulo, ModalityType.APPLICATION_MODAL);
        setContentPane(formulario);
        setSize(new Dimension(620, 520));
        setLocationRelativeTo(pai);
    }

    public PainelCompromisso getFormulario() {
        return formulario;
    }

    @Override
    public void abrir(JComponent tela) {
        setContentPane(tela);
        revalidate();
    }

    @Override
    public void voltar() {
        dispose();
    }
}
