package br.com.athletiza.view.dashboard;

import br.com.athletiza.util.Cores;
import br.com.athletiza.view.Tema;
import br.com.athletiza.view.componentes.Recarregavel;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Dashboard: indicadores da atlética em gráficos. Abre na visão geral e tem abas
 * no topo para Atletas, Treinos, Competições, Eventos e Gestão.
 * Cada aba busca seus dados quando é aberta, para mostrar sempre a informação atual.
 */
public class PainelDashboard extends JPanel implements Recarregavel {

    private final JTabbedPane abas = new JTabbedPane();

    public PainelDashboard() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));

        JLabel titulo = new JLabel("Dashboard");
        titulo.setFont(Tema.fonteTitulo());
        add(titulo, BorderLayout.NORTH);

        abas.putClientProperty(FlatClientProperties.STYLE, "tabType:underlined; underlineColor:" + Cores.hex(Cores.VERDE)
                + "; tabHeight:36; showTabSeparators:false");
        abas.addTab("Visão geral", new AbaVisaoGeral());
        abas.addTab("Atletas", new AbaAtletas());
        abas.addTab("Treinos", new AbaTreinos());
        abas.addTab("Competições", new AbaCompeticoes());
        abas.addTab("Eventos", new AbaEventos());
        abas.addTab("Gestão", new AbaGestao());
        abas.addChangeListener(e -> carregar());
        add(abas, BorderLayout.CENTER);
    }

    /** Atualiza apenas a aba que está aberta. */
    @Override
    public void carregar() {
        if (abas.getSelectedComponent() instanceof Recarregavel aba) {
            aba.carregar();
        }
    }
}
