package br.com.athletiza.view.componentes;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Painel provisório para módulos ainda não implementados.
 */
public class PainelEmConstrucao extends JPanel {

    public PainelEmConstrucao(String modulo, String chamados) {
        super(new GridBagLayout());
        JLabel titulo = new JLabel(modulo);
        titulo.setFont(Tema.fonteTitulo());
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel detalhe = new JLabel("Em construção (" + chamados + ")");
        detalhe.setFont(Tema.fonte(Font.PLAIN, 13f));
        detalhe.setForeground(Cores.TEXTO_SECUNDARIO);
        detalhe.setAlignmentX(CENTER_ALIGNMENT);

        JPanel conteudo = new JPanel();
        conteudo.setOpaque(false);
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.add(titulo);
        conteudo.add(detalhe);
        add(conteudo);
    }
}
